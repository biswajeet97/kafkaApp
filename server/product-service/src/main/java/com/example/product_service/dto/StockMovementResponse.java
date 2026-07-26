package com.example.product_service.dto;

import com.example.product_service.enums.ReferenceType;
import com.example.product_service.enums.StockMovementType;

import java.time.Instant;

public record StockMovementResponse(
        Integer id,
        Integer productId,
        StockMovementType type,
        int quantity,
        int availableQuantityBefore,
        int availableQuantityAfter,
        int reservedQuantityBefore,
        int reservedQuantityAfter,
        ReferenceType referenceType,
        Integer referenceId,
        String reason,
        String createdBy,
        Instant createdAt
) {
}
