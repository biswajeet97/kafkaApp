package com.example.product_service.exception;

public class SkuAlreadyExistsException extends RuntimeException {
    public SkuAlreadyExistsException(String sku) {
        super("A product already exists with SKU: " + sku);
    }
}
