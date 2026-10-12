package com.grocery.assistant.service;

import com.grocery.assistant.dto.*;
import com.grocery.assistant.entity.Product;
import com.grocery.assistant.entity.Sale;
import com.grocery.assistant.entity.SaleItem;
import com.grocery.assistant.repository.ProductRepository;
import com.grocery.assistant.repository.SaleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BillingService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UnitConversionService unitConversionService;
    private final TamilVoiceService tamilVoiceService;

    public BillingService(SaleRepository saleRepository,
                          ProductRepository productRepository,
                          UnitConversionService unitConversionService,
                          TamilVoiceService tamilVoiceService) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.unitConversionService = unitConversionService;
        this.tamilVoiceService = tamilVoiceService;
    }

    /**
     * Completes a sale, freezes item prices, deducts inventory, and returns Tamil total announcement.
     */
    public SaleResponseDto completeSale(SaleRequestDto request, String cashierUsername) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create an empty bill. At least one item is required.");
        }

        // Prevent duplicate submissions via idempotency check
        String saleNumber = request.getSaleNumber();
        if (saleNumber != null && !saleNumber.trim().isEmpty()) {
            Optional<Sale> existing = saleRepository.findBySaleNumber(saleNumber.trim());
            if (existing.isPresent()) {
                // Return already saved sale to guarantee idempotency
                return toResponseDto(existing.get());
            }
        } else {
            saleNumber = generateSaleNumber();
        }

        Sale sale = new Sale();
        sale.setSaleNumber(saleNumber);
        sale.setPaymentMode(request.getPaymentMode() != null ? request.getPaymentMode() : "CASH");
        sale.setCashierUsername(cashierUsername != null ? cashierUsername : "shopkeeper");
        sale.setStatus("COMPLETED");

        BigDecimal runningSubtotal = BigDecimal.ZERO;

        for (SaleItemRequestDto itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + itemReq.getProductId()));

            if (!product.isActive()) {
                throw new IllegalStateException("Product '" + product.getNameEn() + "' is inactive and cannot be sold.");
            }

            // Price freezing policy: Use client price snapshot if supplied, else current product price
            BigDecimal unitPrice = itemReq.getClientPriceSnapshot() != null ?
                    itemReq.getClientPriceSnapshot() : product.getSellingPrice();

            if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Selling price must be greater than zero for product: " + product.getNameEn());
            }

            BigDecimal lineSubtotal = unitConversionService.calculateItemSubtotal(
                    unitPrice,
                    product.getUnit(),
                    itemReq.getQuantity(),
                    itemReq.getUnit() != null ? itemReq.getUnit() : product.getUnit()
            );

            runningSubtotal = runningSubtotal.add(lineSubtotal);

            // Deduct stock
            BigDecimal baseQuantity = unitConversionService.convertQuantityToBaseUnit(
                    itemReq.getQuantity(),
                    itemReq.getUnit() != null ? itemReq.getUnit() : product.getUnit(),
                    product.getUnit()
            );
            BigDecimal newStock = product.getStockQuantity().subtract(baseQuantity);
            if (newStock.compareTo(BigDecimal.ZERO) < 0) {
                newStock = BigDecimal.ZERO; // Prevent negative inventory underflow
            }
            product.setStockQuantity(newStock);
            productRepository.save(product);

            SaleItem item = new SaleItem(
                    product,
                    product.getNameEn(),
                    product.getNameTa(),
                    (itemReq.getUnit() != null ? itemReq.getUnit() : product.getUnit()).name(),
                    unitPrice,
                    itemReq.getQuantity(),
                    lineSubtotal
            );
            sale.addItem(item);
        }

        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }

        BigDecimal totalAmount = runningSubtotal.subtract(discount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        sale.setSubtotal(runningSubtotal.setScale(2, RoundingMode.HALF_UP));
        sale.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));
        sale.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));

        Sale saved = saleRepository.save(sale);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<SaleResponseDto> getSaleById(Long id) {
        return saleRepository.findById(id).map(this::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Optional<SaleResponseDto> getSaleByNumber(String saleNumber) {
        return saleRepository.findBySaleNumber(saleNumber).map(this::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<SaleResponseDto> getSalesHistory(Pageable pageable) {
        return saleRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponseDto);
    }

    public SaleResponseDto toResponseDto(Sale sale) {
        SaleResponseDto dto = new SaleResponseDto();
        dto.setId(sale.getId());
        dto.setSaleNumber(sale.getSaleNumber());
        dto.setSubtotal(sale.getSubtotal());
        dto.setDiscount(sale.getDiscount());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setPaymentMode(sale.getPaymentMode());
        dto.setCashierUsername(sale.getCashierUsername());
        dto.setStatus(sale.getStatus());
        dto.setCreatedAt(sale.getCreatedAt());

        dto.setSpokenTamilTotal(tamilVoiceService.formatBillTotalAnnouncement(sale.getTotalAmount()));

        dto.setItems(sale.getItems().stream().map(item -> {
            SaleItemResponseDto itemDto = new SaleItemResponseDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductNameEn(item.getProductNameEn());
            itemDto.setProductNameTa(item.getProductNameTa());
            itemDto.setUnitCode(item.getUnitCode());
            itemDto.setUnitPriceSnapshot(item.getUnitPriceSnapshot());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setSubtotal(item.getSubtotal());
            return itemDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    private String generateSaleNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "BILL-" + timestamp + "-" + randomSuffix;
    }
}
