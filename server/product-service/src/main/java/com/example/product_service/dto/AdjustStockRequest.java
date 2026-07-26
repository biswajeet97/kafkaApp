package com.example.product_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdjustStockRequest(
        @NotNull Integer quantityChange,
        @Size(max = 500) String reason
) {
}
