package com.example.product_service.service;

import com.example.product_service.dto.*;
import com.example.product_service.entity.Product;
import com.example.product_service.enums.ProductStatus;
import com.example.product_service.enums.ReferenceType;
import com.example.product_service.enums.StockMovementType;
import com.example.product_service.exception.InvalidProductStateException;
import com.example.product_service.exception.ProductNotFoundException;
import com.example.product_service.exception.SkuAlreadyExistsException;
import com.example.product_service.repository.ProductRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepo productRepo;
    private final StockMovementService movementService;

    public ProductService(ProductRepo productRepo, StockMovementService movementService) {
        this.productRepo = productRepo;
        this.movementService = movementService;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        String sku = normalizeSku(request.sku());
        if (productRepo.existsBySkuIgnoreCase(sku)) {
            throw new SkuAlreadyExistsException(sku);
        }

        Product product = Product.builder()
                .sku(sku)
                .productName(request.name().trim())
                .productDescription(trimToNull(request.description()))
                .productPrice(request.price())
                .productCurrency(normalizeCurrency(request.currency()))
                .productAvaQty(request.initialQuantity())
                .productResQty(0)
                .productStatus(ProductStatus.DRAFT)
                .build();

        Product saved = productRepo.save(product);
        if (saved.getProductAvaQty() > 0) {
            movementService.record(saved, StockMovementType.INITIAL_STOCK, saved.getProductAvaQty(),
                    0, 0, ReferenceType.PRODUCT, saved.getProductId(),
                    "Initial product stock", "product-api");
        }
        return toResponse(saved);
    }

    public ProductResponse getProduct(Integer productId) {
        return toResponse(findProduct(productId));
    }

    public ProductResponse getProductBySku(String sku) {
        Product product = productRepo.findBySkuIgnoreCase(normalizeSku(sku))
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        return toResponse(product);
    }

    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepo.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public ProductResponse updateProduct(Integer productId, UpdateProductRequest request) {
        Product product = findProduct(productId);
        rejectDiscontinued(product);
        product.setProductName(request.name().trim());
        product.setProductDescription(trimToNull(request.description()));
        return toResponse(productRepo.save(product));
    }

    @Transactional
    public ProductResponse updatePrice(Integer productId, UpdatePriceRequest request) {
        Product product = findProduct(productId);
        rejectDiscontinued(product);
        product.setProductPrice(request.price());
        if (request.currency() != null) {
            product.setProductCurrency(normalizeCurrency(request.currency()));
        }
        return toResponse(productRepo.save(product));
    }

    @Transactional
    public ProductResponse adjustStock(Integer productId, AdjustStockRequest request) {
        Product product = findProduct(productId);
        rejectDiscontinued(product);
        int availableBefore = product.getProductAvaQty();
        int reservedBefore = product.getProductResQty();
        long adjusted = (long) product.getProductAvaQty() + request.quantityChange();
        if (adjusted < 0) {
            throw new InvalidProductStateException("Available quantity cannot be negative");
        }
        if (adjusted > Integer.MAX_VALUE) {
            throw new InvalidProductStateException("Available quantity exceeds the supported limit");
        }
        product.setProductAvaQty((int) adjusted);
        Product saved = productRepo.save(product);
        movementService.record(saved, StockMovementType.MANUAL_ADJUSTMENT, request.quantityChange(),
                availableBefore, reservedBefore, ReferenceType.PRODUCT, productId,
                request.reason(), "product-api");
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse activateProduct(Integer productId) {
        Product product = findProduct(productId);
        if (product.getProductStatus() != ProductStatus.DRAFT
                && product.getProductStatus() != ProductStatus.INACTIVE) {
            throw new InvalidProductStateException(
                    "Product cannot be activated from status " + product.getProductStatus());
        }
        product.setProductStatus(ProductStatus.ACTIVE);
        return toResponse(productRepo.save(product));
    }

    @Transactional
    public ProductResponse deactivateProduct(Integer productId) {
        Product product = findProduct(productId);
        if (product.getProductStatus() != ProductStatus.ACTIVE) {
            throw new InvalidProductStateException("Only an active product can be deactivated");
        }
        product.setProductStatus(ProductStatus.INACTIVE);
        return toResponse(productRepo.save(product));
    }

    @Transactional
    public ProductResponse discontinueProduct(Integer productId) {
        Product product = findProduct(productId);
        product.setProductStatus(ProductStatus.DISCONTINUED);
        return toResponse(productRepo.save(product));
    }

    private Product findProduct(Integer productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
    }

    private void rejectDiscontinued(Product product) {
        if (product.getProductStatus() == ProductStatus.DISCONTINUED) {
            throw new InvalidProductStateException("A discontinued product cannot be modified");
        }
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "-");
    }

    private String normalizeCurrency(String currency) {
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getSku(),
                product.getProductName(),
                product.getProductDescription(),
                product.getProductPrice(),
                product.getProductCurrency(),
                product.getProductAvaQty(),
                product.getProductResQty(),
                product.getProductStatus(),
                product.getProductVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
