package com.kota.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StockAdjustmentRequest {

    @NotNull
    private Long ingredientId;

    @NotNull
    private BigDecimal quantity;

    @NotBlank
    private String reason;

    @NotBlank
    private String transactionType;
}
