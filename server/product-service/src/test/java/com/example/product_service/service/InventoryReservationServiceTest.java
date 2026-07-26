package com.example.product_service.service;

import com.example.product_service.dto.CreateReservationRequest;
import com.example.product_service.dto.ReservationItemRequest;
import com.example.product_service.entity.InventoryReservation;
import com.example.product_service.entity.InventoryReservationItem;
import com.example.product_service.entity.Product;
import com.example.product_service.enums.ProductStatus;
import com.example.product_service.enums.ReservationStatus;
import com.example.product_service.exception.InsufficientStockException;
import com.example.product_service.repository.InventoryReservationItemRepo;
import com.example.product_service.repository.InventoryReservationRepo;
import com.example.product_service.repository.ProductRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.example.product_service.service.ProductServiceTest.product;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryReservationServiceTest {

    @Mock InventoryReservationRepo reservationRepo;
    @Mock InventoryReservationItemRepo itemRepo;
    @Mock ProductRepo productRepo;
    @Mock StockMovementService movementService;

    private InventoryReservationService service;

    @BeforeEach
    void setUp() {
        service = new InventoryReservationService(reservationRepo, itemRepo, productRepo, movementService);
    }

    @Test
    void reservesAvailableStockTransactionally() {
        Product product = product(1, "PHONE-001", 10, 0, ProductStatus.ACTIVE);
        var request = new CreateReservationRequest(
                1001, Instant.now().plusSeconds(600),
                List.of(new ReservationItemRequest("phone-001", 3)));
        when(reservationRepo.findByOrderId(1001)).thenReturn(Optional.empty());
        when(productRepo.findForUpdateBySkuIgnoreCase("PHONE-001")).thenReturn(Optional.of(product));
        when(reservationRepo.save(any())).thenAnswer(invocation -> {
            InventoryReservation reservation = invocation.getArgument(0);
            reservation.setReservationId(20);
            return reservation;
        });
        when(itemRepo.save(any())).thenAnswer(invocation -> {
            InventoryReservationItem item = invocation.getArgument(0);
            item.setReservationItemId(30);
            return item;
        });
        when(itemRepo.findAllByReservationIdOrderByReservationItemId(20))
                .thenAnswer(invocation -> List.of());

        var response = service.reserve(request);

        assertThat(response.status()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(product.getProductAvaQty()).isEqualTo(7);
        assertThat(product.getProductResQty()).isEqualTo(3);
        verify(productRepo).save(product);
        verify(movementService).record(eq(product), any(), eq(3), eq(10), eq(0),
                any(), eq(20), contains("1001"), eq("inventory-api"));
    }

    @Test
    void rejectsReservationWhenStockIsInsufficient() {
        Product product = product(1, "PHONE-001", 2, 0, ProductStatus.ACTIVE);
        var request = new CreateReservationRequest(
                1001, Instant.now().plusSeconds(600),
                List.of(new ReservationItemRequest("PHONE-001", 3)));
        when(reservationRepo.findByOrderId(1001)).thenReturn(Optional.empty());
        when(productRepo.findForUpdateBySkuIgnoreCase("PHONE-001")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.reserve(request))
                .isInstanceOf(InsufficientStockException.class);
        verify(reservationRepo, never()).save(any());
        verifyNoInteractions(movementService);
    }
}
