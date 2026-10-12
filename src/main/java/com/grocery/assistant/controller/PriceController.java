package com.grocery.assistant.controller;

import com.grocery.assistant.dto.PriceHistoryDto;
import com.grocery.assistant.dto.PriceUpdateDto;
import com.grocery.assistant.dto.ProductDto;
import com.grocery.assistant.service.PriceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @PatchMapping("/api/products/{id}/price")
    public ResponseEntity<ProductDto> updatePrice(@PathVariable Long id,
                                                 @Valid @RequestBody PriceUpdateDto updateDto,
                                                 Authentication auth) {
        String adminUsername = (auth != null) ? auth.getName() : "admin";
        ProductDto updated = priceService.updateProductPrice(id, updateDto, adminUsername);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/api/products/{id}/price-history")
    public ResponseEntity<List<PriceHistoryDto>> getProductPriceHistory(@PathVariable Long id) {
        List<PriceHistoryDto> history = priceService.getPriceHistoryForProduct(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/api/price-history")
    public ResponseEntity<Page<PriceHistoryDto>> getAllPriceHistory(@PageableDefault(size = 20) Pageable pageable) {
        Page<PriceHistoryDto> history = priceService.getAllPriceHistory(pageable);
        return ResponseEntity.ok(history);
    }
}
