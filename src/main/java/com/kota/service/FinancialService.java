package com.kota.service;

import com.kota.dto.response.FinancialSummaryResponse;
import com.kota.model.Order;
import com.kota.model.PaymentTransaction;
import com.kota.repository.OrderRepository;
import com.kota.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public FinancialSummaryResponse getDailyReport(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return buildReport(date, start, end);
    }

    public List<FinancialSummaryResponse> getPeriodReport(LocalDate from, LocalDate to) {
        List<FinancialSummaryResponse> reports = new ArrayList<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            reports.add(getDailyReport(current));
            current = current.plusDays(1);
        }
        return reports;
    }

    private FinancialSummaryResponse buildReport(LocalDate date, LocalDateTime start, LocalDateTime end) {
        List<Order> completedOrders = orderRepository.findByStatus(Order.Status.COMPLETED).stream()
                .filter(o -> o.getCompletedAt() != null && !o.getCompletedAt().isBefore(start) && o.getCompletedAt().isBefore(end))
                .toList();

        BigDecimal totalRevenue = completedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCogs = completedOrders.stream()
                .map(o -> o.getCogsAmount() != null ? o.getCogsAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalYocoFees = paymentTransactionRepository
                .findByStatusAndCreatedAtBetween(PaymentTransaction.Status.SUCCEEDED, start, end)
                .stream()
                .map(pt -> pt.getYocoFee() != null ? pt.getYocoFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossProfit = totalRevenue.subtract(totalCogs);
        BigDecimal netProfit = grossProfit.subtract(totalYocoFees);

        FinancialSummaryResponse report = new FinancialSummaryResponse();
        report.setDate(date);
        report.setTotalRevenue(totalRevenue);
        report.setTotalCogs(totalCogs);
        report.setTotalYocoFees(totalYocoFees);
        report.setGrossProfit(grossProfit);
        report.setNetProfit(netProfit);
        report.setTotalOrders(completedOrders.size());
        return report;
    }

    public BigDecimal calculateCOGS(Order order) {
        return order.getOrderItems().stream()
                .map(item -> item.getCogsPerItem() != null
                        ? item.getCogsPerItem().multiply(BigDecimal.valueOf(item.getQuantity()))
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
