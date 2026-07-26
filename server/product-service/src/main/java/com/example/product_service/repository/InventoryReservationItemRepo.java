package com.example.product_service.repository;

import com.example.product_service.entity.InventoryReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryReservationItemRepo extends JpaRepository<InventoryReservationItem, Integer> {
    List<InventoryReservationItem> findAllByReservationIdOrderByReservationItemId(Integer reservationId);
}
