package com.example.product_service.service;

import com.example.product_service.dto.StockMovementResponse;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.StockMovement;
import com.example.product_service.enums.ReferenceType;
import com.example.product_service.enums.StockMovementType;
import com.example.product_service.exception.ProductNotFoundException;
import com.example.product_service.repository.ProductRepo;
import com.example.product_service.repository.StockMovementRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StockMovementService {

    private final StockMovementRepo movementRepo;
    private final ProductRepo productRepo;

    public StockMovementService(StockMovementRepo movementRepo, ProductRepo productRepo) {
        this.movementRepo = movementRepo;
        this.productRepo = productRepo;
    }

    public Page<StockMovementResponse> getAll(Pageable pageable) {
        return movementRepo.findAll(pageable).map(this::toResponse);
    }

    public Page<StockMovementResponse> getByProduct(Integer productId, Pageable pageable) {
        if (!productRepo.existsById(productId)) {
            throw new ProductNotFoundException("Product not found: " + productId);
        }
        return movementRepo.findAllByProductId(productId, pageable).map(this::toResponse);
    }

    @Transactional
    public void record(
            Product product,
            StockMovementType type,
            int quantity,
            int availableBefore,
            int reservedBefore,
            ReferenceType referenceType,
            Integer referenceId,
            String reason,
            String createdBy) {
        movementRepo.save(StockMovement.builder()
                .productId(product.getProductId())
                .type(type)
                .quantity(quantity)
                .availableQuantityBefore(availableBefore)
                .availableQuantityAfter(product.getProductAvaQty())
                .reservedQuantityBefore(reservedBefore)
                .reservedQuantityAfter(product.getProductResQty())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .reason(reason)
                .createdBy(createdBy)
                .build());
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(), movement.getProductId(), movement.getType(), movement.getQuantity(),
                movement.getAvailableQuantityBefore(), movement.getAvailableQuantityAfter(),
                movement.getReservedQuantityBefore(), movement.getReservedQuantityAfter(),
                movement.getReferenceType(), movement.getReferenceId(), movement.getReason(),
                movement.getCreatedBy(), movement.getCreatedAt());
    }
}
