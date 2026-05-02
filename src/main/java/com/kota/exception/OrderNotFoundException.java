package com.kota.exception;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long id) {
        super("Order", id);
    }

    public OrderNotFoundException(String orderNumber) {
        super("Order not found with number: " + orderNumber);
    }
}
