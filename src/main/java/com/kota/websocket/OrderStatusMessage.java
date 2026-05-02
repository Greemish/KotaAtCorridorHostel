package com.kota.websocket;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderStatusMessage {

    private Long orderId;
    private String orderNumber;
    private String status;
    private Integer queuePosition;
    private String message;
    private LocalDateTime timestamp;
}
