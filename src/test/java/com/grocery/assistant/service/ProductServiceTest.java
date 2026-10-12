package com.grocery.assistant.service;

import com.grocery.assistant.dto.ProductDto;
import com.grocery.assistant.entity.Product;
import com.grocery.assistant.entity.enums.ProductType;
import com.grocery.assistant.entity.enums.Unit;
import com.grocery.assistant.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TamilVoiceService tamilVoiceService;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setNameEn("Ponni Rice");
        product.setNameTa("பொன்னி அரிசி");
        product.setBarcode("8901030000101");
        product.setCategory("Grains");
        product.setProductType(ProductType.LOOSE);
        product.setUnit(Unit.KG);
        product.setPurchasePrice(new BigDecimal("45.00"));
        product.setSellingPrice(new BigDecimal("55.00"));
        product.setStockQuantity(new BigDecimal("100.000"));
        product.setLowStockThreshold(new BigDecimal("10.000"));
        product.setActive(true);
    }

    @Test
    @DisplayName("Create product successfully")
    void testCreateProduct() {
        ProductDto dto = new ProductDto();
        dto.setNameEn("Sugar");
        dto.setNameTa("சர்க்கரை");
        dto.setCategory("Essentials");
        dto.setBarcode("8901030000102");
        dto.setProductType(ProductType.LOOSE);
        dto.setUnit(Unit.KG);
        dto.setSellingPrice(new BigDecimal("48.00"));
        dto.setPurchasePrice(new BigDecimal("40.00"));
        dto.setStockQuantity(new BigDecimal("50.000"));
        dto.setLowStockThreshold(new BigDecimal("5.000"));

        when(productRepository.existsByBarcode("8901030000102")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        ProductDto created = productService.createProduct(dto);

        assertNotNull(created);
        assertEquals(2L, created.getId());
        assertEquals("Sugar", created.getNameEn());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Reject product creation with duplicate barcode")
    void testRejectDuplicateBarcode() {
        ProductDto dto = new ProductDto();
        dto.setBarcode("8901030000101");

        when(productRepository.existsByBarcode("8901030000101")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(dto)
        );
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Soft delete sets active to false")
    void testDeleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertFalse(product.isActive());
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Find product by barcode")
    void testFindByBarcode() {
        when(productRepository.findByBarcodeAndActiveTrue("8901030000101")).thenReturn(Optional.of(product));

        Optional<ProductDto> result = productService.getProductByBarcode("8901030000101");

        assertTrue(result.isPresent());
        assertEquals("Ponni Rice", result.get().getNameEn());
    }
}
