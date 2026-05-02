package com.kota.service;

import com.kota.model.Ingredient;
import com.kota.model.Notification;
import com.kota.model.Order;
import com.kota.model.User;
import com.kota.repository.NotificationRepository;
import com.kota.repository.UserRepository;
import com.kota.websocket.OrderStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void sendOrderStatusUpdate(Order order) {
        OrderStatusMessage message = new OrderStatusMessage();
        message.setOrderId(order.getId());
        message.setOrderNumber(order.getOrderNumber());
        message.setStatus(order.getStatus().name());
        message.setQueuePosition(order.getQueuePosition());
        message.setMessage("Order status updated to " + order.getStatus().name());
        message.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/orders/" + order.getId(), message);
        saveNotification(order.getStudent().getId(), message.getMessage(), "ORDER_STATUS", order.getId());
    }

    public void sendNewOrderToStaff(Order order) {
        OrderStatusMessage message = new OrderStatusMessage();
        message.setOrderId(order.getId());
        message.setOrderNumber(order.getOrderNumber());
        message.setStatus(order.getStatus().name());
        message.setMessage("New order received: " + order.getOrderNumber());
        message.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/staff/orders", message);
    }

    public void sendLowStockAlert(Ingredient ingredient) {
        String alertMsg = "Low stock alert: " + ingredient.getName() + " (" + ingredient.getCurrentStock() + " " + ingredient.getUnit() + " remaining)";
        messagingTemplate.convertAndSend("/topic/admin/alerts", alertMsg);
        log.warn(alertMsg);
    }

    @Transactional
    public void saveNotification(Long userId, String message, String type, Long relatedOrderId) {
        userRepository.findById(userId).ifPresent(user -> {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setType(type);
            notification.setRelatedOrderId(relatedOrderId);
            notificationRepository.save(notification);
        });
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
