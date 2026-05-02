package com.kota.controller;

import com.kota.dto.request.*;
import com.kota.dto.response.*;
import com.kota.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final MenuService menuService;
    private final InventoryService inventoryService;
    private final FinancialService financialService;
    private final PaymentService paymentService;
    private final AuditService auditService;

    // User management
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        userService.deleteUser(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Menu management
    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems() {
        return ResponseEntity.ok(menuService.getAllItems());
    }

    @PostMapping("/menu")
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody CreateMenuItemRequest request,
                                                           @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(menuService.createItem(request, user.getUsername()));
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Long id,
                                                           @Valid @RequestBody CreateMenuItemRequest request,
                                                           @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(menuService.updateItem(id, request, user.getUsername()));
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        menuService.deleteItem(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/menu/{id}/recipe")
    public ResponseEntity<Void> updateRecipe(@PathVariable Long id,
                                             @RequestBody List<RecipeRequest> recipes,
                                             @AuthenticationPrincipal UserDetails user) {
        menuService.updateRecipe(id, recipes, user.getUsername());
        return ResponseEntity.ok().build();
    }

    // Inventory
    @GetMapping("/inventory")
    public ResponseEntity<List<IngredientResponse>> getInventory() {
        return ResponseEntity.ok(inventoryService.getAllIngredients());
    }

    @PostMapping("/inventory")
    public ResponseEntity<IngredientResponse> createIngredient(@Valid @RequestBody CreateIngredientRequest request,
                                                               @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(inventoryService.createIngredient(request, user.getUsername()));
    }

    @PutMapping("/inventory/{id}")
    public ResponseEntity<IngredientResponse> updateIngredient(@PathVariable Long id,
                                                               @Valid @RequestBody CreateIngredientRequest request,
                                                               @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(inventoryService.updateIngredient(id, request, user.getUsername()));
    }

    @PostMapping("/inventory/adjust")
    public ResponseEntity<IngredientResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request,
                                                          @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(inventoryService.adjustStock(request, user.getUsername()));
    }

    // Financials
    @GetMapping("/financials/daily")
    public ResponseEntity<FinancialSummaryResponse> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(financialService.getDailyReport(date));
    }

    @GetMapping("/financials/period")
    public ResponseEntity<List<FinancialSummaryResponse>> getPeriodReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(financialService.getPeriodReport(from, to));
    }

    // Payments
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentTransactionResponse>> getPayments() {
        return ResponseEntity.ok(paymentService.getPaymentTransactions());
    }

    @PostMapping("/payments/refund")
    public ResponseEntity<Void> processRefund(@Valid @RequestBody ProcessRefundRequest request,
                                              @AuthenticationPrincipal UserDetails user) {
        paymentService.processRefund(request, user.getUsername());
        return ResponseEntity.ok().build();
    }

    // Audit logs
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(Pageable pageable) {
        Page<com.kota.model.AuditLog> page = auditService.getAllLogs(pageable);
        List<AuditLogResponse> list = page.getContent().stream().map(log -> {
            AuditLogResponse r = new AuditLogResponse();
            r.setId(log.getId());
            r.setAction(log.getAction());
            r.setEntityType(log.getEntityType());
            r.setOldValue(log.getOldValue());
            r.setNewValue(log.getNewValue());
            r.setReason(log.getReason());
            r.setPerformedBy(log.getPerformedBy() != null ? log.getPerformedBy().getEmail() : null);
            r.setCreatedAt(log.getCreatedAt());
            return r;
        }).toList();
        return ResponseEntity.ok(list);
    }
}
