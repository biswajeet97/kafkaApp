package com.example.product_service.controller;

import com.example.product_service.dto.ReservationItemResponse;
import com.example.product_service.service.InventoryReservationItemService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/reservations/{reservationId}/items")
@Validated
public class InventoryReservationItemController {

    private final InventoryReservationItemService itemService;

    public InventoryReservationItemController(InventoryReservationItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ReservationItemResponse> getItems(
            @PathVariable @Positive Integer reservationId) {
        return itemService.getByReservation(reservationId);
    }
}
