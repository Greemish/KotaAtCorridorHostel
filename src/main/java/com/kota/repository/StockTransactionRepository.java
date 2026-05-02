package com.kota.repository;

import com.kota.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findByIngredientId(Long ingredientId);

    List<StockTransaction> findByOrderId(Long orderId);
}
