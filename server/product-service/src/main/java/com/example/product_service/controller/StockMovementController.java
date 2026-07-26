package com.example.product_service.controller;

import com.example.product_service.dto.StockMovementResponse;
import com.example.product_service.service.StockMovementService;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/stock-movements")
@Validated
public class StockMovementController {

    private final StockMovementService movementService;

    public StockMovementController(StockMovementService movementService) {
        this.movementService = movementService;
    }

    @GetMapping
    public Page<StockMovementResponse> getAll(
            @RequestParam(required = false) @Positive Integer productId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return productId == null
                ? movementService.getAll(pageable)
                : movementService.getByProduct(productId, pageable);
    }
}
