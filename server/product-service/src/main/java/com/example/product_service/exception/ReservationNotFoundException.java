package com.example.product_service.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(Integer reservationId) {
        super("Inventory reservation not found: " + reservationId);
    }
}
