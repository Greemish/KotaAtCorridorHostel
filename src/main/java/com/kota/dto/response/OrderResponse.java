package com.kota.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private Integer queuePosition;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private String studentName;
}
