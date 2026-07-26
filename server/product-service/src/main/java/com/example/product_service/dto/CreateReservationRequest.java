package com.example.product_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;

public record CreateReservationRequest(
        @NotNull @Positive Integer orderId,
        @NotNull @Future Instant expiresAt,
        @NotEmpty List<@Valid ReservationItemRequest> items
) {
}
