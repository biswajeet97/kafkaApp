package com.example.product_service.dto;

import com.example.product_service.enums.ReservationStatus;

import java.time.Instant;
import java.util.List;

public record ReservationResponse(
        Integer reservationId,
        Integer orderId,
        ReservationStatus status,
        Instant expiresAt,
        String failureReason,
        Instant createdAt,
        Instant updatedAt,
        Instant confirmedAt,
        Instant releasedAt,
        Long version,
        List<ReservationItemResponse> items
) {
}
