package com.kota.service;

import com.kota.dto.request.CreateOrderRequest;
import com.kota.dto.request.ReorderQueueRequest;
import com.kota.dto.request.UpdateOrderStatusRequest;
import com.kota.dto.response.OrderResponse;
import com.kota.dto.response.OrderStatusResponse;
import com.kota.exception.BusinessException;
import com.kota.exception.OrderNotFoundException;
import com.kota.exception.ResourceNotFoundException;
import com.kota.model.MenuItem;
import com.kota.model.Order;
import com.kota.model.OrderItem;
import com.kota.model.Recipe;
import com.kota.model.User;
import com.kota.repository.MenuItemRepository;
import com.kota.repository.OrderRepository;
import com.kota.repository.RecipeRepository;
import com.kota.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final QueueService queueService;
    private final AuditService auditService;

    @Value("${app.order.throttle-window-minutes:10}")
    private int throttleWindowMinutes;

    @Value("${app.order.throttle-max-orders:30}")
    private int throttleMaxOrders;

    @Value("${app.order.max-pending-orders:20}")
    private int maxPendingOrders;

    @Value("${app.order.auto-cancel-minutes:30}")
    private int autoCancelMinutes;

    @Value("${app.operational.open-hour:10}")
    private int openHour;

    @Value("${app.operational.close-hour:22}")
    private int closeHour;

    private static final AtomicInteger orderSequence = new AtomicInteger(0);

    @Transactional
    public String createOrder(CreateOrderRequest request, Long studentId) {
        checkOperationalHours();
        checkThrottle();
        checkQueueSize();

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        Order order = new Order();
        order.setStudent(student);
        order.setNotes(request.getNotes());
        order.setStatus(Order.Status.PENDING_PAYMENT);

        BigDecimal total = BigDecimal.ZERO;
        for (var itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemRequest.getMenuItemId()));
            if (!menuItem.isAvailable()) {
                throw new BusinessException("Menu item not available: " + menuItem.getName());
            }
            if (!inventoryService.checkStockAvailability(menuItem.getId(), itemRequest.getQuantity())) {
                throw new BusinessException("Insufficient stock for: " + menuItem.getName());
            }
            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(menuItem.getPrice());
            orderItem.setCogsPerItem(calculateItemCOGS(menuItem));
            order.getOrderItems().add(orderItem);
        }

        order.setTotalAmount(total);
        order.setOrderNumber(generateOrderNumber());
        Order savedOrder = orderRepository.save(order);

        inventoryService.reserveStock(savedOrder);
        return paymentService.createCheckoutSession(savedOrder);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<OrderResponse> getMyOrders(Long studentId) {
        return orderRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OrderStatusResponse getOrderStatus(Long orderId, Long studentId) {
        Order order = getOrderById(orderId);
        if (!order.getStudent().getId().equals(studentId)) {
            throw new BusinessException("Access denied to this order");
        }
        return toStatusResponse(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long studentId) {
        Order order = getOrderById(orderId);
        if (!order.getStudent().getId().equals(studentId)) {
            throw new BusinessException("Access denied to this order");
        }
        if (order.getStatus() == Order.Status.PREPARING || order.getStatus() == Order.Status.READY
                || order.getStatus() == Order.Status.COMPLETED) {
            throw new BusinessException("Cannot cancel order in status: " + order.getStatus());
        }
        Order.Status previousStatus = order.getStatus();
        if (previousStatus == Order.Status.PAID) {
            inventoryService.returnStock(order);
        }
        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);
        notificationService.sendOrderStatusUpdate(order);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, String performedBy) {
        Order order = getOrderById(orderId);
        Order.Status oldStatus = order.getStatus();
        Order.Status newStatus = Order.Status.valueOf(request.getStatus());

        order.setStatus(newStatus);

        if (newStatus == Order.Status.PREPARING) {
            inventoryService.deductStock(order);
        } else if (newStatus == Order.Status.READY) {
            order.setReadyAt(LocalDateTime.now());
        } else if (newStatus == Order.Status.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        } else if (newStatus == Order.Status.CANCELLED) {
            if (oldStatus == Order.Status.PAID || oldStatus == Order.Status.PREPARING) {
                inventoryService.returnStock(order);
            }
        }

        if (newStatus == Order.Status.PAID) {
            queueService.assignQueuePosition(order);
        }

        orderRepository.save(order);
        notificationService.sendOrderStatusUpdate(order);
        auditService.log("ORDER_STATUS_CHANGE", "Order", orderId,
                oldStatus.name(), newStatus.name(), request.getReason(), null,
                userRepository.findByEmail(performedBy).map(User::getId).orElse(null));
    }

    public List<OrderResponse> getPaidOrders() {
        return orderRepository.findByStatusIn(List.of(Order.Status.PAID, Order.Status.PREPARING, Order.Status.READY))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getQueueOrders() {
        return queueService.getCurrentQueue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void reorderQueue(ReorderQueueRequest request, String performedBy) {
        queueService.reorderQueue(request.getOrderedOrderIds(), performedBy);
        auditService.log("QUEUE_REORDER", "Queue", null, null,
                request.getOrderedOrderIds().toString(), request.getReason(), null,
                userRepository.findByEmail(performedBy).map(User::getId).orElse(null));
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelReadyOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(autoCancelMinutes);
        List<Order> readyOrders = orderRepository.findByStatus(Order.Status.READY);
        for (Order order : readyOrders) {
            if (order.getReadyAt() != null && order.getReadyAt().isBefore(cutoff)) {
                order.setStatus(Order.Status.CANCELLED);
                orderRepository.save(order);
                notificationService.sendOrderStatusUpdate(order);
                log.info("Auto-cancelled ready order {} after {} minutes", order.getOrderNumber(), autoCancelMinutes);
            }
        }
    }

    private void checkOperationalHours() {
        int currentHour = LocalDateTime.now().getHour();
        if (currentHour < openHour || currentHour >= closeHour) {
            throw new BusinessException("Ordering is only available between " + openHour + ":00 and " + closeHour + ":00");
        }
    }

    private void checkThrottle() {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(throttleWindowMinutes);
        long recentOrders = orderRepository.countByStatusAndCreatedAtAfter(Order.Status.PENDING_PAYMENT, windowStart)
                + orderRepository.countByStatusAndCreatedAtAfter(Order.Status.PAID, windowStart);
        if (recentOrders >= throttleMaxOrders) {
            throw new BusinessException("System is experiencing high demand. Please try again in a few minutes.");
        }
    }

    private void checkQueueSize() {
        List<Order> pendingOrders = orderRepository.findByStatusIn(
                List.of(Order.Status.PAID, Order.Status.PREPARING));
        if (pendingOrders.size() >= maxPendingOrders) {
            throw new BusinessException("Queue is full. Please try again later.");
        }
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = orderSequence.incrementAndGet() % 10000;
        return String.format("KOTA-%s-%04d", datePart, seq);
    }

    private BigDecimal calculateItemCOGS(MenuItem menuItem) {
        return recipeRepository.findByMenuItemId(menuItem.getId()).stream()
                .map(r -> r.getIngredient().getCostPerUnit().multiply(r.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OrderResponse toResponse(Order order) {
        OrderResponse r = new OrderResponse();
        r.setId(order.getId());
        r.setOrderNumber(order.getOrderNumber());
        r.setStatus(order.getStatus().name());
        r.setTotalAmount(order.getTotalAmount());
        r.setQueuePosition(order.getQueuePosition());
        r.setCreatedAt(order.getCreatedAt());
        r.setStudentName(order.getStudent().getFirstName() + " " + order.getStudent().getLastName());
        r.setItems(order.getOrderItems().stream().map(item -> {
            var ir = new com.kota.dto.response.OrderItemResponse();
            ir.setMenuItemId(item.getMenuItem().getId());
            ir.setMenuItemName(item.getMenuItem().getName());
            ir.setQuantity(item.getQuantity());
            ir.setUnitPrice(item.getUnitPrice());
            return ir;
        }).collect(Collectors.toList()));
        return r;
    }

    private OrderStatusResponse toStatusResponse(Order order) {
        OrderStatusResponse r = new OrderStatusResponse();
        r.setOrderId(order.getId());
        r.setOrderNumber(order.getOrderNumber());
        r.setStatus(order.getStatus().name());
        r.setQueuePosition(order.getQueuePosition());
        if (order.getQueuePosition() != null) {
            int avgMinutes = 10;
            r.setEstimatedWaitMinutes(order.getQueuePosition() * avgMinutes);
        }
        return r;
    }
}
