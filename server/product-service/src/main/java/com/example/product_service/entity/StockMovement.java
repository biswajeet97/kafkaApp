package com.example.product_service.entity;

import com.example.product_service.enums.ReferenceType;
import com.example.product_service.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "stock_movement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockMovementType type;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int availableQuantityBefore;

    @Column(nullable = false)
    private int availableQuantityAfter;

    @Column(nullable = false)
    private int reservedQuantityBefore;

    @Column(nullable = false)
    private int reservedQuantityAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReferenceType referenceType;

    private Integer referenceId;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, length = 100)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
