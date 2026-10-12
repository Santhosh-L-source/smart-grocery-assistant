package com.grocery.assistant.service;

import com.grocery.assistant.dto.ProductDto;
import com.grocery.assistant.entity.Product;
import com.grocery.assistant.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final TamilVoiceService tamilVoiceService;

    public ProductService(ProductRepository productRepository, TamilVoiceService tamilVoiceService) {
        this.productRepository = productRepository;
        this.tamilVoiceService = tamilVoiceService;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllActiveProducts() {
        return productRepository.findByActiveTrueOrderByNameEnAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getActiveProductsPaged(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<ProductDto> getProductByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return Optional.empty();
        }
        return productRepository.findByBarcodeAndActiveTrue(barcode.trim()).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveProducts();
        }
        return productRepository.searchActiveProducts(query.trim()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProductDto createProduct(ProductDto dto) {
        if (dto.getBarcode() != null && !dto.getBarcode().trim().isEmpty()) {
            if (productRepository.existsByBarcode(dto.getBarcode().trim())) {
                throw new IllegalArgumentException("A product with barcode " + dto.getBarcode() + " already exists");
            }
        }

        Product product = new Product();
        updateEntityFromDto(product, dto);
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        if (dto.getBarcode() != null && !dto.getBarcode().trim().isEmpty()) {
            if (productRepository.existsByBarcodeAndIdNot(dto.getBarcode().trim(), id)) {
                throw new IllegalArgumentException("Barcode " + dto.getBarcode() + " is already used by another product");
            }
        }

        updateEntityFromDto(product, dto);
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    public ProductDto toDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setNameEn(p.getNameEn());
        dto.setNameTa(p.getNameTa());
        dto.setBarcode(p.getBarcode());
        dto.setCategory(p.getCategory());
        dto.setProductType(p.getProductType());
        dto.setUnit(p.getUnit());
        dto.setPurchasePrice(p.getPurchasePrice());
        dto.setSellingPrice(p.getSellingPrice());
        dto.setStockQuantity(p.getStockQuantity());
        dto.setLowStockThreshold(p.getLowStockThreshold());
        dto.setActive(p.isActive());
        dto.setVersion(p.getVersion());
        dto.setImageUrl(p.getImageUrl());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        // Generate natural Tamil spoken announcement
        dto.setSpokenTamilPrice(tamilVoiceService.formatProductPriceAnnouncement(
                p.getNameTa(), p.getNameEn(), p.getUnit(), p.getSellingPrice()
        ));

        return dto;
    }

    private void updateEntityFromDto(Product p, ProductDto dto) {
        p.setNameEn(dto.getNameEn().trim());
        p.setNameTa(dto.getNameTa() != null ? dto.getNameTa().trim() : null);
        p.setBarcode((dto.getBarcode() != null && !dto.getBarcode().trim().isEmpty()) ? dto.getBarcode().trim() : null);
        p.setCategory(dto.getCategory().trim());
        p.setProductType(dto.getProductType());
        p.setUnit(dto.getUnit());
        p.setPurchasePrice(dto.getPurchasePrice());
        p.setSellingPrice(dto.getSellingPrice());
        p.setStockQuantity(dto.getStockQuantity());
        p.setLowStockThreshold(dto.getLowStockThreshold());
        p.setActive(dto.isActive());
        p.setImageUrl(dto.getImageUrl());
    }
}
