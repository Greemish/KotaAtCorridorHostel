package com.kota.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StockAlertResponse {

    private Long ingredientId;
    private String name;
    private BigDecimal currentStock;
    private BigDecimal reorderPoint;
    private String unit;
}
