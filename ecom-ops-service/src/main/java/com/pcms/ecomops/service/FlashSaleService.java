package com.pcms.ecomops.service;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.ecomops.dto.request.CreateFlashSaleRequest;
import com.pcms.ecomops.dto.request.FlashSaleItemRequest;
import com.pcms.ecomops.dto.response.FlashSaleItemResponse;
import com.pcms.ecomops.dto.response.FlashSaleResponse;
import com.pcms.ecomops.entity.FlashSale;
import com.pcms.ecomops.entity.FlashSaleItem;
import com.pcms.ecomops.repository.FlashSaleItemRepository;
import com.pcms.ecomops.repository.FlashSaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FlashSaleService {

    private final FlashSaleRepository repository;
    private final FlashSaleItemRepository itemRepository;

    public FlashSaleService(FlashSaleRepository repository, FlashSaleItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public FlashSaleResponse create(CreateFlashSaleRequest request) {
        FlashSale sale = new FlashSale();
        sale.setName(request.name());
        sale.setDescription(request.description());
        sale.setStartsAt(request.startsAt());
        sale.setEndsAt(request.endsAt());
        sale.setDiscountPct(request.discountPct());
        sale.setMaxQtyPerUser(request.maxQtyPerUser() != null ? request.maxQtyPerUser() : 1);
        sale.setStatus("SCHEDULED");
        sale = repository.save(sale);

        // Create items
        if (request.items() != null) {
            for (FlashSaleItemRequest itemReq : request.items()) {
                FlashSaleItem item = new FlashSaleItem();
                item.setFlashSaleId(sale.getId());
                item.setMedicineId(itemReq.medicineId());
                item.setOriginalPrice(itemReq.originalPrice());
                item.setSalePrice(itemReq.salePrice());
                item.setQtyLimit(itemReq.qtyLimit());
                item.setSoldQty(0);
                itemRepository.save(item);
            }
        }

        return toResponse(sale);
    }

    @Transactional(readOnly = true)
    public List<FlashSaleResponse> listActive() {
        return repository.findByStatusOrderByStartsAtAsc("ACTIVE").stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FlashSaleResponse> listAll() {
        return repository.findAllByOrderByStartsAtDesc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FlashSaleResponse get(UUID id) {
        FlashSale sale = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FlashSale", id));
        return toResponse(sale);
    }

    @Transactional
    public FlashSaleResponse cancel(UUID id) {
        FlashSale sale = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FlashSale", id));
        sale.setStatus("CANCELLED");
        return toResponse(repository.save(sale));
    }

    /**
     * Cron-style: activate SCHEDULED sales that have started, end ACTIVE sales past their end time.
     * Called by FlashSaleScheduler (not shown for brevity).
     */
    @Transactional
    public void updateStatuses() {
        LocalDateTime now = LocalDateTime.now();
        repository.findAll().forEach(sale -> {
            if ("SCHEDULED".equals(sale.getStatus()) && !now.isBefore(sale.getStartsAt())) {
                sale.setStatus("ACTIVE");
                repository.save(sale);
            } else if ("ACTIVE".equals(sale.getStatus()) && now.isAfter(sale.getEndsAt())) {
                sale.setStatus("ENDED");
                repository.save(sale);
            }
        });
    }

    private FlashSaleResponse toResponse(FlashSale sale) {
        List<FlashSaleItem> items = itemRepository.findByFlashSaleId(sale.getId());
        List<FlashSaleItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();
        return new FlashSaleResponse(
                sale.getId(), sale.getName(), sale.getDescription(),
                sale.getStartsAt(), sale.getEndsAt(), sale.getDiscountPct(),
                sale.getMaxQtyPerUser(), sale.getStatus(), itemResponses);
    }

    private FlashSaleItemResponse toItemResponse(FlashSaleItem item) {
        int discountPct = 0;
        if (item.getOriginalPrice() != null
                && item.getOriginalPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            discountPct = item.getOriginalPrice()
                    .subtract(item.getSalePrice())
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .divide(item.getOriginalPrice(), 0, RoundingMode.HALF_UP)
                    .intValue();
        }
        return new FlashSaleItemResponse(
                item.getId(), item.getFlashSaleId(), item.getMedicineId(),
                item.getMedicineName() != null ? item.getMedicineName() : "",
                item.getImageUrl() != null ? item.getImageUrl() : "",
                item.getOriginalPrice(), item.getSalePrice(),
                discountPct,
                item.getQtyLimit(), item.getSoldQty());
    }
}
