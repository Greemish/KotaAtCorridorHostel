package com.kota.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FinancialSummaryResponse {

    private LocalDate date;
    private BigDecimal totalRevenue;
    private BigDecimal totalCogs;
    private BigDecimal totalYocoFees;
    private BigDecimal grossProfit;
    private BigDecimal netProfit;
    private long totalOrders;
}
