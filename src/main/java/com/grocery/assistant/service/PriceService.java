package com.grocery.assistant.service;

import com.grocery.assistant.dto.PriceHistoryDto;
import com.grocery.assistant.dto.PriceUpdateDto;
import com.grocery.assistant.dto.ProductDto;
import com.grocery.assistant.entity.PriceHistory;
import com.grocery.assistant.entity.Product;
import com.grocery.assistant.entity.enums.UpdateMethod;
import com.grocery.assistant.repository.PriceHistoryRepository;
import com.grocery.assistant.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PriceService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductService productService;

    public PriceService(ProductRepository productRepository,
                        PriceHistoryRepository priceHistoryRepository,
                        ProductService productService) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.productService = productService;
    }

    public ProductDto updateProductPrice(Long productId, PriceUpdateDto updateDto, String adminUsername) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        // Optimistic locking check if client provided version
        if (updateDto.getExpectedVersion() != null && !updateDto.getExpectedVersion().equals(product.getVersion())) {
            throw new IllegalStateException("Price update conflict: The product price was modified by another administrator. Please refresh.");
        }

        BigDecimal oldSellingPrice = product.getSellingPrice();
        BigDecimal newSellingPrice = updateDto.getNewSellingPrice();
        if (newSellingPrice == null || newSellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New selling price must be greater than zero");
        }

        BigDecimal oldPurchasePrice = product.getPurchasePrice();
        BigDecimal newPurchasePrice = updateDto.getNewPurchasePrice() != null ? updateDto.getNewPurchasePrice() : oldPurchasePrice;

        // Apply new prices
        product.setSellingPrice(newSellingPrice);
        if (updateDto.getNewPurchasePrice() != null) {
            product.setPurchasePrice(newPurchasePrice);
        }

        Product savedProduct = productRepository.save(product);

        // Record audit trail in price_history
        UpdateMethod method = updateDto.getUpdateMethod() != null ? updateDto.getUpdateMethod() : UpdateMethod.MANUAL;
        String reason = updateDto.getChangeReason();
        if (reason == null || reason.trim().isEmpty()) {
            reason = (method == UpdateMethod.VOICE) ? "Voice command price update" : "Manual price update";
        }

        PriceHistory history = new PriceHistory(
                savedProduct,
                oldSellingPrice,
                newSellingPrice,
                oldPurchasePrice,
                newPurchasePrice,
                method,
                adminUsername != null ? adminUsername : "admin",
                reason
        );
        priceHistoryRepository.save(history);

        return productService.toDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<PriceHistoryDto> getPriceHistoryForProduct(Long productId) {
        return priceHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::toHistoryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PriceHistoryDto> getAllPriceHistory(Pageable pageable) {
        return priceHistoryRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toHistoryDto);
    }

    private PriceHistoryDto toHistoryDto(PriceHistory h) {
        PriceHistoryDto dto = new PriceHistoryDto();
        dto.setId(h.getId());
        dto.setProductId(h.getProduct().getId());
        dto.setProductNameEn(h.getProduct().getNameEn());
        dto.setProductNameTa(h.getProduct().getNameTa());
        dto.setOldSellingPrice(h.getOldSellingPrice());
        dto.setNewSellingPrice(h.getNewSellingPrice());
        dto.setOldPurchasePrice(h.getOldPurchasePrice());
        dto.setNewPurchasePrice(h.getNewPurchasePrice());
        dto.setUpdateMethod(h.getUpdateMethod());
        dto.setAdminUsername(h.getAdminUsername());
        dto.setChangeReason(h.getChangeReason());
        dto.setCreatedAt(h.getCreatedAt());
        return dto;
    }
}
