package com.kota.service;

import com.kota.dto.response.QueueResponse;
import com.kota.model.Order;
import com.kota.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final OrderRepository orderRepository;

    public List<Order> getCurrentQueue() {
        return orderRepository.findByStatusIn(
                List.of(Order.Status.PAID, Order.Status.PREPARING)
        );
    }

    @Transactional
    public int assignQueuePosition(Order order) {
        List<Order> activeOrders = getCurrentQueue();
        int position = activeOrders.size() + 1;
        order.setQueuePosition(position);
        return position;
    }

    @Transactional
    public void reorderQueue(List<Long> orderedIds, String performedBy) {
        List<Order> queue = getCurrentQueue();
        for (int i = 0; i < orderedIds.size(); i++) {
            final int position = i + 1;
            final Long orderId = orderedIds.get(i);
            queue.stream()
                    .filter(o -> o.getId().equals(orderId))
                    .findFirst()
                    .ifPresent(o -> {
                        o.setQueuePosition(position);
                        orderRepository.save(o);
                    });
        }
        log.info("Queue reordered by {}", performedBy);
    }

    public Integer getQueuePosition(Long orderId) {
        return orderRepository.findById(orderId)
                .map(Order::getQueuePosition)
                .orElse(null);
    }

    public QueueResponse buildQueueResponse() {
        List<Order> orders = getCurrentQueue();
        orders.sort((a, b) -> {
            if (a.getQueuePosition() == null && b.getQueuePosition() == null) return 0;
            if (a.getQueuePosition() == null) return 1;
            if (b.getQueuePosition() == null) return -1;
            return a.getQueuePosition().compareTo(b.getQueuePosition());
        });

        List<QueueResponse.OrderSummary> summaries = orders.stream().map(o -> {
            QueueResponse.OrderSummary s = new QueueResponse.OrderSummary();
            s.setId(o.getId());
            s.setOrderNumber(o.getOrderNumber());
            s.setStatus(o.getStatus().name());
            s.setQueuePosition(o.getQueuePosition());
            s.setStudentName(o.getStudent().getFirstName() + " " + o.getStudent().getLastName());
            s.setTotalAmount(o.getTotalAmount());
            s.setCreatedAt(o.getCreatedAt());
            return s;
        }).toList();

        QueueResponse response = new QueueResponse();
        response.setOrders(summaries);
        response.setTotalPending(summaries.size());
        return response;
    }
}
