package com.example.product_service.controller;

import com.example.product_service.dto.ReservationItemResponse;
import com.example.product_service.exception.ApiError;
import com.example.product_service.service.InventoryReservationItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/reservations/{reservationId}/items")
@Validated
@Tag(name = "Reservation Items", description = "Items belonging to inventory reservations")
public class InventoryReservationItemController {

    private final InventoryReservationItemService itemService;

    public InventoryReservationItemController(InventoryReservationItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    @Operation(summary = "List items in a reservation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation items returned"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation ID", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<ReservationItemResponse> getItems(
            @PathVariable @Positive Integer reservationId) {
        return itemService.getByReservation(reservationId);
    }
}
