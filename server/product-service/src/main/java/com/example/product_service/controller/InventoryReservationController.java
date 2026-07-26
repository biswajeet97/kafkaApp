package com.example.product_service.controller;

import com.example.product_service.dto.CreateReservationRequest;
import com.example.product_service.dto.ReservationResponse;
import com.example.product_service.service.InventoryReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/inventory/reservations")
@Validated
public class InventoryReservationController {

    private final InventoryReservationService reservationService;

    public InventoryReservationController(InventoryReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse reservation = reservationService.reserve(request);
        return ResponseEntity
                .created(URI.create("/api/v1/inventory/reservations/" + reservation.reservationId()))
                .body(reservation);
    }

    @GetMapping("/{reservationId}")
    public ReservationResponse get(@PathVariable @Positive Integer reservationId) {
        return reservationService.get(reservationId);
    }

    @GetMapping
    public Page<ReservationResponse> getAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return reservationService.getAll(pageable);
    }

    @PostMapping("/{reservationId}/confirm")
    public ReservationResponse confirm(@PathVariable @Positive Integer reservationId) {
        return reservationService.confirm(reservationId);
    }

    @PostMapping("/{reservationId}/release")
    public ReservationResponse release(@PathVariable @Positive Integer reservationId) {
        return reservationService.release(reservationId);
    }
}
