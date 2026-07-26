package com.example.product_service.controller;

import com.example.product_service.dto.StockMovementResponse;
import com.example.product_service.exception.ApiError;
import com.example.product_service.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/stock-movements")
@Validated
@Tag(name = "Stock Movements", description = "Audit trail of inventory quantity changes")
public class StockMovementController {

    private final StockMovementService movementService;

    public StockMovementController(StockMovementService movementService) {
        this.movementService = movementService;
    }

    @GetMapping
    @Operation(summary = "List stock movements", description = "Returns all movements, or filters them by product ID when supplied.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movements returned"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public Page<StockMovementResponse> getAll(
            @RequestParam(required = false) @Positive Integer productId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return productId == null
                ? movementService.getAll(pageable)
                : movementService.getByProduct(productId, pageable);
    }
}
