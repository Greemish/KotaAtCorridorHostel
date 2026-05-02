package com.kota.repository;

import com.kota.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByOrderId(Long orderId);

    Optional<PaymentTransaction> findByYocoCheckoutId(String yocoCheckoutId);

    Optional<PaymentTransaction> findByYocoPaymentId(String yocoPaymentId);
}
