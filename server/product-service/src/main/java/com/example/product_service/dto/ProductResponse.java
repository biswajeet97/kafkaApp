package com.example.product_service.dto;

import com.example.product_service.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Integer productId,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        int availableQuantity,
        int reservedQuantity,
        ProductStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
