package com.kota.repository;

import com.kota.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findByActiveTrue();

    @Query("SELECT i FROM Ingredient i WHERE i.currentStock < i.reorderPoint AND i.active = true")
    List<Ingredient> findByCurrentStockLessThanReorderPoint();
}
