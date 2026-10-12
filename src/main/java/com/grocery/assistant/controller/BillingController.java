package com.grocery.assistant.controller;

import com.grocery.assistant.dto.SaleRequestDto;
import com.grocery.assistant.dto.SaleResponseDto;
import com.grocery.assistant.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping
    public ResponseEntity<SaleResponseDto> completeSale(@Valid @RequestBody SaleRequestDto request, Authentication auth) {
        String cashier = (auth != null) ? auth.getName() : "shopkeeper";
        SaleResponseDto response = billingService.completeSale(request, cashier);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponseDto> getSaleById(@PathVariable Long id) {
        return billingService.getSaleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<SaleResponseDto>> getSalesHistory(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(billingService.getSalesHistory(pageable));
    }
}
