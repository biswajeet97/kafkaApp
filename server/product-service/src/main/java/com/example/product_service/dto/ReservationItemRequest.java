package com.example.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReservationItemRequest(
        @NotBlank @Size(max = 64) String sku,
        @Positive int quantity
) {
}
