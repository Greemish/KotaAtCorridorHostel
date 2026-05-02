package com.kota.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecipeRequest {

    @NotNull
    private Long ingredientId;

    @NotNull
    private BigDecimal quantity;
}
