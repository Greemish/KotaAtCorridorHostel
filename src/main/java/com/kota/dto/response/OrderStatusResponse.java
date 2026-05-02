package com.kota.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusResponse {

    private Long orderId;
    private String orderNumber;
    private String status;
    private Integer queuePosition;
    private Integer estimatedWaitMinutes;
}
