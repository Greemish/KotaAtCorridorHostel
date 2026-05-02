package com.kota.repository;

import com.kota.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStudentId(Long studentId);

    List<Order> findByStatus(Order.Status status);

    List<Order> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    long countByStatusAndCreatedAtAfter(Order.Status status, LocalDateTime after);

    List<Order> findByStatusIn(List<Order.Status> statuses);

    Optional<Order> findByOrderNumber(String orderNumber);
}
