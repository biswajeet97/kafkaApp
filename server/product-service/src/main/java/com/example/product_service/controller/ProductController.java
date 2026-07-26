package com.example.product_service.controller;

import com.example.product_service.dto.*;
import com.example.product_service.exception.ApiError;
import com.example.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
@Validated
@Tag(name = "Products", description = "Product catalog, pricing, stock, and lifecycle operations")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create a product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "SKU already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity
                .created(URI.create("/api/v1/products/" + product.productId()))
                .body(product);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get a product by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse getById(@PathVariable @Positive Integer productId) {
        return productService.getProduct(productId);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get a product by SKU")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "400", description = "Invalid SKU", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse getBySku(@PathVariable @NotBlank String sku) {
        return productService.getProductBySku(sku);
    }

    @GetMapping
    @Operation(summary = "List products", description = "Returns a pageable product list.")
    @ApiResponse(responseCode = "200", description = "Products returned")
    public Page<ProductResponse> getAll(@PageableDefault(size = 20, sort = "productName") Pageable pageable) {
        return productService.getProducts(pageable);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update product details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Product state does not allow the update", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse update(
            @PathVariable @Positive Integer productId,
            @Valid @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(productId, request);
    }

    @PatchMapping("/{productId}/price")
    @Operation(summary = "Update a product price")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Price updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Product state does not allow the update", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse updatePrice(
            @PathVariable @Positive Integer productId,
            @Valid @RequestBody UpdatePriceRequest request) {
        return productService.updatePrice(productId, request);
    }

    @PatchMapping("/{productId}/stock")
    @Operation(summary = "Adjust available stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock adjusted"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Insufficient stock or invalid product state", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse adjustStock(
            @PathVariable @Positive Integer productId,
            @Valid @RequestBody AdjustStockRequest request) {
        return productService.adjustStock(productId, request);
    }

    @PostMapping("/{productId}/activate")
    @Operation(summary = "Activate a product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product activated"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Invalid product state transition", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse activate(@PathVariable @Positive Integer productId) {
        return productService.activateProduct(productId);
    }

    @PostMapping("/{productId}/deactivate")
    @Operation(summary = "Deactivate a product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deactivated"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Invalid product state transition", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse deactivate(@PathVariable @Positive Integer productId) {
        return productService.deactivateProduct(productId);
    }

    @PostMapping("/{productId}/discontinue")
    @Operation(summary = "Discontinue a product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product discontinued"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Invalid product state transition", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse discontinue(@PathVariable @Positive Integer productId) {
        return productService.discontinueProduct(productId);
    }
}
