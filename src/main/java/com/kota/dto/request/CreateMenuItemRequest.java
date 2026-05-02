package com.kota.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateMenuItemRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    private int preparationMinutes = 10;

    private String imageUrl;
}
