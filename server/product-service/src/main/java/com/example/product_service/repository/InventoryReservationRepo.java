package com.example.product_service.repository;

import com.example.product_service.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryReservationRepo extends JpaRepository<InventoryReservation, Integer> {
    Optional<InventoryReservation> findByOrderId(Integer orderId);
}
