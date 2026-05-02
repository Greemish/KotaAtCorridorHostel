package com.kota.exception;

public class StockInsufficientException extends BusinessException {

    public StockInsufficientException(String ingredientName) {
        super("Insufficient stock for ingredient: " + ingredientName);
    }
}
