package com.example.product_service.dto;

import java.time.Instant;

public record ReservationItemResponse(
        Integer reservationItemId,
        Integer reservationId,
        Integer productId,
        String sku,
        int quantity,
        Instant createdAt
) {
}
