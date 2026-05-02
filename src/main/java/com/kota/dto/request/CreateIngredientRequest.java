package com.kota.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateIngredientRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String unit;

    @NotNull
    private BigDecimal currentStock;

    @NotNull
    private BigDecimal reorderPoint;

    @NotNull
    private BigDecimal reorderQuantity;

    @NotNull
    private BigDecimal costPerUnit;
}
