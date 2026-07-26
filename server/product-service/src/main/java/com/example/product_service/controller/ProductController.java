package com.example.product_service.controller;

import com.example.product_service.dto.*;
import com.example.product_service.service.ProductService;
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
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity
                .created(URI.create("/api/v1/products/" + product.productId()))
                .body(product);
    }

    @GetMapping("/{productId}")
    public ProductResponse getById(@PathVariable @Positive Integer productId) {
        return productService.getProduct(productId);
    }

    @GetMapping("/sku/{sku}")
    public ProductResponse getBySku(@PathVariable @NotBlank String sku) {
        return productService.getProductBySku(sku);
    }

    @GetMapping
    public Page<ProductResponse> getAll(@PageableDefault(size = 20, sort = "productName") Pageable pageable) {
        return productService.getProducts(pageable);
    }

    @PutMapping("/{productId}")
    public ProductResponse update(
            @PathVariable @Positive Integer productId,
            @Valid @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(productId, request);
    }

    @PatchMapping("/{productId}/price")
    public ProductResponse updatePrice(
            @PathVariable @Positive Integer productId,
            @Valid @RequestBody UpdatePriceRequest request) {
        return productService.updatePrice(productId, request);
    }

    @PatchMapping("/{productId}/stock")
    public ProductResponse adjustStock(
            @PathVariable @Positive Integer productId,
            @Valid @RequestBody AdjustStockRequest request) {
        return productService.adjustStock(productId, request);
    }

    @PostMapping("/{productId}/activate")
    public ProductResponse activate(@PathVariable @Positive Integer productId) {
        return productService.activateProduct(productId);
    }

    @PostMapping("/{productId}/deactivate")
    public ProductResponse deactivate(@PathVariable @Positive Integer productId) {
        return productService.deactivateProduct(productId);
    }

    @PostMapping("/{productId}/discontinue")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse discontinue(@PathVariable @Positive Integer productId) {
        return productService.discontinueProduct(productId);
    }
}
