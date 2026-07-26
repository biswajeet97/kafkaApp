package com.example.product_service.service;

import com.example.product_service.dto.AdjustStockRequest;
import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.entity.Product;
import com.example.product_service.enums.ProductStatus;
import com.example.product_service.exception.InvalidProductStateException;
import com.example.product_service.exception.SkuAlreadyExistsException;
import com.example.product_service.repository.ProductRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private StockMovementService movementService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepo, movementService);
    }

    @Test
    void createsProductWithNormalizedValuesAndInitialMovement() {
        CreateProductRequest request = new CreateProductRequest(
                " phone 001 ", " Phone ", " Flagship ", new BigDecimal("499.99"), "usd", 10);
        when(productRepo.existsBySkuIgnoreCase("PHONE-001")).thenReturn(false);
        when(productRepo.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setProductId(1);
            return product;
        });

        var response = productService.createProduct(request);

        assertThat(response.sku()).isEqualTo("PHONE-001");
        assertThat(response.name()).isEqualTo("Phone");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.status()).isEqualTo(ProductStatus.DRAFT);
        verify(movementService).record(any(Product.class), any(), eq(10), eq(0), eq(0),
                any(), eq(1), eq("Initial product stock"), eq("product-api"));
    }

    @Test
    void rejectsDuplicateSku() {
        when(productRepo.existsBySkuIgnoreCase("PHONE-001")).thenReturn(true);
        CreateProductRequest request = new CreateProductRequest(
                "phone-001", "Phone", null, BigDecimal.TEN, "USD", 0);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(SkuAlreadyExistsException.class);
        verify(productRepo, never()).save(any());
    }

    @Test
    void rejectsStockAdjustmentThatWouldBecomeNegative() {
        Product product = product(1, "PHONE-001", 3, 0, ProductStatus.ACTIVE);
        when(productRepo.findById(1)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.adjustStock(1, new AdjustStockRequest(-4, "damage")))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("cannot be negative");
        verify(productRepo, never()).save(any());
        verifyNoInteractions(movementService);
    }

    static Product product(Integer id, String sku, int available, int reserved, ProductStatus status) {
        return Product.builder()
                .productId(id)
                .sku(sku)
                .productName("Phone")
                .productPrice(BigDecimal.TEN)
                .productCurrency("USD")
                .productAvaQty(available)
                .productResQty(reserved)
                .productStatus(status)
                .build();
    }
}
