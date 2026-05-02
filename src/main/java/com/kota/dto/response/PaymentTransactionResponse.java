package com.kota.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentTransactionResponse {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private BigDecimal yocoFee;
    private String status;
    private String cardLast4;
    private LocalDateTime createdAt;
}
