package com.kota.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class QueueResponse {

    @Getter
    @Setter
    public static class OrderSummary {
        private Long id;
        private String orderNumber;
        private String status;
        private Integer queuePosition;
        private String studentName;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
    }

    private List<OrderSummary> orders;
    private int totalPending;
}
