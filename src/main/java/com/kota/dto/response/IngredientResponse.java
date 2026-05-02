package com.kota.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IngredientResponse {

    private Long id;
    private String name;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal reorderPoint;
    private BigDecimal costPerUnit;
    private boolean lowStock;
}
