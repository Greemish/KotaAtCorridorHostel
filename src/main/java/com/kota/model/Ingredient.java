package com.kota.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ingredients")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(name = "current_stock", nullable = false, precision = 10, scale = 3)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "reorder_point", nullable = false, precision = 10, scale = 3)
    private BigDecimal reorderPoint = BigDecimal.ZERO;

    @Column(name = "reorder_quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal reorderQuantity = BigDecimal.ZERO;

    @Column(name = "cost_per_unit", nullable = false, precision = 10, scale = 4)
    private BigDecimal costPerUnit = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
