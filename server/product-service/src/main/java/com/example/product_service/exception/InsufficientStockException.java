package com.example.product_service.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String sku, int requested, int available) {
        super("Insufficient stock for SKU %s: requested %d, available %d"
                .formatted(sku, requested, available));
    }
}
