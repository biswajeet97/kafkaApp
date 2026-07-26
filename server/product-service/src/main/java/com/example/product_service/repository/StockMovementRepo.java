package com.example.product_service.repository;

import com.example.product_service.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface StockMovementRepo extends JpaRepository<StockMovement, Integer> {
    Page<StockMovement> findAllByProductId(Integer productId, Pageable pageable);
}
