package com.kota.controller;

import com.kota.dto.request.ReorderQueueRequest;
import com.kota.dto.request.StockAdjustmentRequest;
import com.kota.dto.request.UpdateOrderStatusRequest;
import com.kota.dto.response.IngredientResponse;
import com.kota.dto.response.OrderResponse;
import com.kota.dto.response.QueueResponse;
import com.kota.dto.response.StockAlertResponse;
import com.kota.service.InventoryService;
import com.kota.service.OrderService;
import com.kota.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
@RequiredArgsConstructor
public class StaffController {

    private final OrderService orderService;
    private final QueueService queueService;
    private final InventoryService inventoryService;

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getActiveOrders() {
        return ResponseEntity.ok(orderService.getPaidOrders());
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserDetails user) {
        orderService.updateOrderStatus(id, request, user.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/queue")
    public ResponseEntity<QueueResponse> getQueue() {
        return ResponseEntity.ok(queueService.buildQueueResponse());
    }

    @PutMapping("/queue/reorder")
    public ResponseEntity<Void> reorderQueue(
            @Valid @RequestBody ReorderQueueRequest request,
            @AuthenticationPrincipal UserDetails user) {
        orderService.reorderQueue(request, user.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stock/alerts")
    public ResponseEntity<List<StockAlertResponse>> getStockAlerts() {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts());
    }

    @PostMapping("/stock/adjust")
    public ResponseEntity<IngredientResponse> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(inventoryService.adjustStock(request, user.getUsername()));
    }
}
