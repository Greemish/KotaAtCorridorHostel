package com.kota.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {

    @NotNull
    private Long menuItemId;

    @NotNull
    private Integer quantity;
}
