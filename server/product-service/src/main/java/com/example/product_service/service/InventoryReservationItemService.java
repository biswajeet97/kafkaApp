package com.example.product_service.service;

import com.example.product_service.dto.ReservationItemResponse;
import com.example.product_service.entity.InventoryReservationItem;
import com.example.product_service.exception.ReservationNotFoundException;
import com.example.product_service.repository.InventoryReservationItemRepo;
import com.example.product_service.repository.InventoryReservationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class InventoryReservationItemService {

    private final InventoryReservationItemRepo itemRepo;
    private final InventoryReservationRepo reservationRepo;

    public InventoryReservationItemService(
            InventoryReservationItemRepo itemRepo, InventoryReservationRepo reservationRepo) {
        this.itemRepo = itemRepo;
        this.reservationRepo = reservationRepo;
    }

    public List<ReservationItemResponse> getByReservation(Integer reservationId) {
        if (!reservationRepo.existsById(reservationId)) {
            throw new ReservationNotFoundException(reservationId);
        }
        return itemRepo.findAllByReservationIdOrderByReservationItemId(reservationId)
                .stream().map(this::toResponse).toList();
    }

    ReservationItemResponse toResponse(InventoryReservationItem item) {
        return new ReservationItemResponse(
                item.getReservationItemId(), item.getReservationId(), item.getProductId(),
                item.getSku(), item.getQuantity(), item.getCreatedAt());
    }
}
