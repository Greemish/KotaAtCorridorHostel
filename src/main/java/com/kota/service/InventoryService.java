package com.kota.service;

import com.kota.dto.request.CreateIngredientRequest;
import com.kota.dto.request.StockAdjustmentRequest;
import com.kota.dto.response.IngredientResponse;
import com.kota.dto.response.StockAlertResponse;
import com.kota.exception.ResourceNotFoundException;
import com.kota.model.Ingredient;
import com.kota.model.Order;
import com.kota.model.OrderItem;
import com.kota.model.Recipe;
import com.kota.model.StockTransaction;
import com.kota.repository.IngredientRepository;
import com.kota.repository.RecipeRepository;
import com.kota.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<StockAlertResponse> getLowStockAlerts() {
        return ingredientRepository.findByCurrentStockLessThanReorderPoint().stream()
                .map(i -> {
                    StockAlertResponse r = new StockAlertResponse();
                    r.setIngredientId(i.getId());
                    r.setName(i.getName());
                    r.setCurrentStock(i.getCurrentStock());
                    r.setReorderPoint(i.getReorderPoint());
                    r.setUnit(i.getUnit());
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public IngredientResponse createIngredient(CreateIngredientRequest request, String performedBy) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName());
        ingredient.setUnit(request.getUnit());
        ingredient.setCurrentStock(request.getCurrentStock());
        ingredient.setReorderPoint(request.getReorderPoint());
        ingredient.setReorderQuantity(request.getReorderQuantity());
        ingredient.setCostPerUnit(request.getCostPerUnit());
        return toResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientResponse updateIngredient(Long id, CreateIngredientRequest request, String performedBy) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", id));
        ingredient.setName(request.getName());
        ingredient.setUnit(request.getUnit());
        ingredient.setReorderPoint(request.getReorderPoint());
        ingredient.setReorderQuantity(request.getReorderQuantity());
        ingredient.setCostPerUnit(request.getCostPerUnit());
        return toResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientResponse adjustStock(StockAdjustmentRequest request, String performedBy) {
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", request.getIngredientId()));
        BigDecimal previous = ingredient.getCurrentStock();
        StockTransaction.TransactionType type = StockTransaction.TransactionType.valueOf(request.getTransactionType());
        BigDecimal newStock;
        if (type == StockTransaction.TransactionType.PURCHASE || type == StockTransaction.TransactionType.RETURN) {
            newStock = previous.add(request.getQuantity());
        } else {
            newStock = previous.subtract(request.getQuantity());
        }
        ingredient.setCurrentStock(newStock);
        ingredientRepository.save(ingredient);
        recordStockTransaction(ingredient, type, request.getQuantity(), previous, newStock, request.getReason(), null, performedBy);
        return toResponse(ingredient);
    }

    @Transactional
    public void recordPurchase(Long ingredientId, BigDecimal quantity, String performedBy) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", ingredientId));
        BigDecimal previous = ingredient.getCurrentStock();
        BigDecimal newStock = previous.add(quantity);
        ingredient.setCurrentStock(newStock);
        ingredientRepository.save(ingredient);
        recordStockTransaction(ingredient, StockTransaction.TransactionType.PURCHASE, quantity, previous, newStock, "Stock purchase", null, performedBy);
    }

    public boolean checkStockAvailability(Long menuItemId, int quantity) {
        List<Recipe> recipes = recipeRepository.findByMenuItemId(menuItemId);
        for (Recipe recipe : recipes) {
            BigDecimal required = recipe.getQuantity().multiply(BigDecimal.valueOf(quantity));
            if (recipe.getIngredient().getCurrentStock().compareTo(required) < 0) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public void reserveStock(Order order) {
        // Stock reservation is tracked but not deducted until PREPARING
        log.info("Stock reserved for order {}", order.getOrderNumber());
    }

    @Transactional
    public void deductStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            List<Recipe> recipes = recipeRepository.findByMenuItemId(item.getMenuItem().getId());
            for (Recipe recipe : recipes) {
                Ingredient ingredient = recipe.getIngredient();
                BigDecimal required = recipe.getQuantity().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal previous = ingredient.getCurrentStock();
                BigDecimal newStock = previous.subtract(required);
                ingredient.setCurrentStock(newStock.max(BigDecimal.ZERO));
                ingredientRepository.save(ingredient);
                recordStockTransaction(ingredient, StockTransaction.TransactionType.SALE, required, previous, ingredient.getCurrentStock(), "Order: " + order.getOrderNumber(), order, "SYSTEM");
            }
        }
    }

    @Transactional
    public void returnStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            List<Recipe> recipes = recipeRepository.findByMenuItemId(item.getMenuItem().getId());
            for (Recipe recipe : recipes) {
                Ingredient ingredient = recipe.getIngredient();
                BigDecimal returned = recipe.getQuantity().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal previous = ingredient.getCurrentStock();
                BigDecimal newStock = previous.add(returned);
                ingredient.setCurrentStock(newStock);
                ingredientRepository.save(ingredient);
                recordStockTransaction(ingredient, StockTransaction.TransactionType.RETURN, returned, previous, newStock, "Cancelled order: " + order.getOrderNumber(), order, "SYSTEM");
            }
        }
    }

    private void recordStockTransaction(Ingredient ingredient, StockTransaction.TransactionType type,
                                        BigDecimal quantity, BigDecimal previous, BigDecimal newStock,
                                        String reason, Order order, String performedBy) {
        StockTransaction tx = new StockTransaction();
        tx.setIngredient(ingredient);
        tx.setTransactionType(type);
        tx.setQuantity(quantity);
        tx.setPreviousStock(previous);
        tx.setNewStock(newStock);
        tx.setReason(reason);
        tx.setOrder(order);
        tx.setCreatedBy(performedBy);
        stockTransactionRepository.save(tx);
    }

    private IngredientResponse toResponse(Ingredient i) {
        IngredientResponse r = new IngredientResponse();
        r.setId(i.getId());
        r.setName(i.getName());
        r.setUnit(i.getUnit());
        r.setCurrentStock(i.getCurrentStock());
        r.setReorderPoint(i.getReorderPoint());
        r.setCostPerUnit(i.getCostPerUnit());
        r.setLowStock(i.getCurrentStock().compareTo(i.getReorderPoint()) < 0);
        return r;
    }
}
