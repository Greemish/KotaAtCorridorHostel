package com.kota.controller;

import com.kota.dto.request.CreateOrderRequest;
import com.kota.dto.response.OrderResponse;
import com.kota.dto.response.OrderStatusResponse;
import com.kota.security.UserPrincipal;
import com.kota.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String redirectUrl = orderService.createOrder(request, principal.getId());
        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getMyOrders(principal.getId()));
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getOrderStatus(id, principal.getId()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        orderService.cancelOrder(id, principal.getId());
        return ResponseEntity.ok().build();
    }
}
