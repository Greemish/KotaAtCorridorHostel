package com.kota.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MenuItemResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean available;
    private int preparationMinutes;
    private String imageUrl;
}
