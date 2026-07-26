package com.example.product_service.repository;

import com.example.product_service.entity.Product;
import com.example.product_service.enums.ProductStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepoTest {

    @Autowired
    private ProductRepo productRepo;

    @Test
    void findsSkuIgnoringCase() {
        productRepo.saveAndFlush(Product.builder()
                .sku("PHONE-001")
                .productName("Phone")
                .productPrice(new BigDecimal("499.99"))
                .productCurrency("USD")
                .productAvaQty(10)
                .productResQty(0)
                .productStatus(ProductStatus.ACTIVE)
                .build());

        assertThat(productRepo.existsBySkuIgnoreCase("phone-001")).isTrue();
        assertThat(productRepo.findBySkuIgnoreCase("phone-001"))
                .get().extracting(Product::getProductName).isEqualTo("Phone");
    }
}
