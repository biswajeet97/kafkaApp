package com.example.product_service.controller;

import com.example.product_service.dto.CreateReservationRequest;
import com.example.product_service.dto.ReservationResponse;
import com.example.product_service.exception.ApiError;
import com.example.product_service.service.InventoryReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Inventory Reservations", description = "Reserve, confirm, and release inventory")
public class InventoryReservationController {

    private final InventoryReservationService reservationService;

    public InventoryReservationController(InventoryReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create an inventory reservation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inventory reserved"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "A requested product was not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Insufficient stock or invalid product state", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ReservationResponse> reserve(
            @Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse reservation = reservationService.reserve(request);
        return ResponseEntity
                .created(URI.create("/api/v1/inventory/reservations/" + reservation.reservationId()))
                .body(reservation);
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Get a reservation by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation ID", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ReservationResponse get(@PathVariable @Positive Integer reservationId) {
        return reservationService.get(reservationId);
    }

    @GetMapping
    @Operation(summary = "List inventory reservations", description = "Returns a pageable reservation list.")
    @ApiResponse(responseCode = "200", description = "Reservations returned")
    public Page<ReservationResponse> getAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return reservationService.getAll(pageable);
    }

    @PostMapping("/{reservationId}/confirm")
    @Operation(summary = "Confirm an inventory reservation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation confirmed"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation ID", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Invalid reservation state", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ReservationResponse confirm(@PathVariable @Positive Integer reservationId) {
        return reservationService.confirm(reservationId);
    }

    @PostMapping("/{reservationId}/release")
    @Operation(summary = "Release an inventory reservation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation released"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation ID", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Invalid reservation state", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ReservationResponse release(@PathVariable @Positive Integer reservationId) {
        return reservationService.release(reservationId);
    }
}
