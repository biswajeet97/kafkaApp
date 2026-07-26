package com.example.product_service.service;

import com.example.product_service.dto.*;
import com.example.product_service.entity.InventoryReservation;
import com.example.product_service.entity.InventoryReservationItem;
import com.example.product_service.entity.Product;
import com.example.product_service.enums.*;
import com.example.product_service.exception.*;
import com.example.product_service.repository.InventoryReservationItemRepo;
import com.example.product_service.repository.InventoryReservationRepo;
import com.example.product_service.repository.ProductRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class InventoryReservationService {

    private final InventoryReservationRepo reservationRepo;
    private final InventoryReservationItemRepo itemRepo;
    private final ProductRepo productRepo;
    private final StockMovementService movementService;

    public InventoryReservationService(
            InventoryReservationRepo reservationRepo,
            InventoryReservationItemRepo itemRepo,
            ProductRepo productRepo,
            StockMovementService movementService) {
        this.reservationRepo = reservationRepo;
        this.itemRepo = itemRepo;
        this.productRepo = productRepo;
        this.movementService = movementService;
    }

    @Transactional
    public ReservationResponse reserve(CreateReservationRequest request) {
        if (reservationRepo.findByOrderId(request.orderId()).isPresent()) {
            throw new InvalidProductStateException(
                    "A reservation already exists for order " + request.orderId());
        }

        List<ResolvedItem> resolvedItems = request.items().stream()
                .map(item -> resolveAndLock(item, request.items()))
                .toList();

        InventoryReservation reservation = reservationRepo.save(InventoryReservation.builder()
                .orderId(request.orderId())
                .status(ReservationStatus.RESERVED)
                .expiresAt(request.expiresAt())
                .build());

        for (ResolvedItem resolved : resolvedItems) {
            Product product = resolved.product();
            int availableBefore = product.getProductAvaQty();
            int reservedBefore = product.getProductResQty();
            product.setProductAvaQty(availableBefore - resolved.quantity());
            product.setProductResQty(reservedBefore + resolved.quantity());
            productRepo.save(product);

            itemRepo.save(InventoryReservationItem.builder()
                    .reservationId(reservation.getReservationId())
                    .productId(product.getProductId())
                    .sku(product.getSku())
                    .quantity(resolved.quantity())
                    .build());

            movementService.record(product, StockMovementType.RESERVATION, resolved.quantity(),
                    availableBefore, reservedBefore, ReferenceType.RESERVATION,
                    reservation.getReservationId(), "Stock reserved for order " + request.orderId(), "inventory-api");
        }
        return toResponse(reservation);
    }

    public ReservationResponse get(Integer reservationId) {
        return toResponse(findReservation(reservationId));
    }

    public Page<ReservationResponse> getAll(Pageable pageable) {
        return reservationRepo.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public ReservationResponse confirm(Integer reservationId) {
        InventoryReservation reservation = findReservation(reservationId);
        requireReserved(reservation);
        if (!reservation.getExpiresAt().isAfter(Instant.now())) {
            throw new InvalidProductStateException("The reservation has expired and cannot be confirmed");
        }

        for (InventoryReservationItem item : getItems(reservationId)) {
            Product product = lockProduct(item.getSku());
            int availableBefore = product.getProductAvaQty();
            int reservedBefore = product.getProductResQty();
            ensureReservedQuantity(product, item);
            product.setProductResQty(reservedBefore - item.getQuantity());
            productRepo.save(product);
            movementService.record(product, StockMovementType.RESERVATION_CONFIRMATION, item.getQuantity(),
                    availableBefore, reservedBefore, ReferenceType.RESERVATION, reservationId,
                    "Reservation confirmed", "inventory-api");
        }
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(Instant.now());
        return toResponse(reservationRepo.save(reservation));
    }

    @Transactional
    public ReservationResponse release(Integer reservationId) {
        InventoryReservation reservation = findReservation(reservationId);
        requireReserved(reservation);

        for (InventoryReservationItem item : getItems(reservationId)) {
            Product product = lockProduct(item.getSku());
            int availableBefore = product.getProductAvaQty();
            int reservedBefore = product.getProductResQty();
            ensureReservedQuantity(product, item);
            product.setProductAvaQty(availableBefore + item.getQuantity());
            product.setProductResQty(reservedBefore - item.getQuantity());
            productRepo.save(product);
            movementService.record(product, StockMovementType.RESERVATION_RELEASE, item.getQuantity(),
                    availableBefore, reservedBefore, ReferenceType.RESERVATION, reservationId,
                    "Reservation released", "inventory-api");
        }
        reservation.setStatus(reservation.getExpiresAt().isAfter(Instant.now())
                ? ReservationStatus.RELEASED : ReservationStatus.EXPIRED);
        reservation.setReleasedAt(Instant.now());
        return toResponse(reservationRepo.save(reservation));
    }

    private ResolvedItem resolveAndLock(ReservationItemRequest item, List<ReservationItemRequest> allItems) {
        String sku = normalizeSku(item.sku());
        long quantity = allItems.stream()
                .filter(candidate -> normalizeSku(candidate.sku()).equals(sku))
                .mapToLong(ReservationItemRequest::quantity)
                .sum();
        if (allItems.stream().filter(candidate -> normalizeSku(candidate.sku()).equals(sku)).count() > 1) {
            throw new InvalidProductStateException("Duplicate SKU in reservation request: " + sku);
        }
        Product product = lockProduct(sku);
        if (product.getProductStatus() != ProductStatus.ACTIVE) {
            throw new InvalidProductStateException("Product must be active to reserve stock: " + sku);
        }
        if (quantity > product.getProductAvaQty()) {
            throw new InsufficientStockException(sku, (int) quantity, product.getProductAvaQty());
        }
        return new ResolvedItem(product, (int) quantity);
    }

    private Product lockProduct(String sku) {
        return productRepo.findForUpdateBySkuIgnoreCase(normalizeSku(sku))
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
    }

    private InventoryReservation findReservation(Integer id) {
        return reservationRepo.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }

    private List<InventoryReservationItem> getItems(Integer reservationId) {
        return itemRepo.findAllByReservationIdOrderByReservationItemId(reservationId);
    }

    private void requireReserved(InventoryReservation reservation) {
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new InvalidProductStateException(
                    "Reservation cannot be changed from status " + reservation.getStatus());
        }
    }

    private void ensureReservedQuantity(Product product, InventoryReservationItem item) {
        if (product.getProductResQty() < item.getQuantity()) {
            throw new InvalidProductStateException("Reserved stock is inconsistent for SKU " + item.getSku());
        }
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "-");
    }

    private ReservationResponse toResponse(InventoryReservation reservation) {
        List<ReservationItemResponse> items = getItems(reservation.getReservationId()).stream()
                .map(item -> new ReservationItemResponse(
                        item.getReservationItemId(), item.getReservationId(), item.getProductId(),
                        item.getSku(), item.getQuantity(), item.getCreatedAt()))
                .toList();
        return new ReservationResponse(
                reservation.getReservationId(), reservation.getOrderId(), reservation.getStatus(),
                reservation.getExpiresAt(), reservation.getFailureReason(), reservation.getCreatedAt(),
                reservation.getUpdatedAt(), reservation.getConfirmedAt(), reservation.getReleasedAt(),
                reservation.getVersion(), items);
    }

    private record ResolvedItem(Product product, int quantity) {
    }
}
