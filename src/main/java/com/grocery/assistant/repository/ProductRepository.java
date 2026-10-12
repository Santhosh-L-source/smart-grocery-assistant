package com.grocery.assistant.repository;

import com.grocery.assistant.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByBarcode(String barcode);

    Optional<Product> findByBarcodeAndActiveTrue(String barcode);

    List<Product> findByActiveTrueOrderByNameEnAsc();

    Page<Product> findByActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND (" +
           "LOWER(p.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.nameTa) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.barcode = :query)")
    List<Product> searchActiveProducts(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStockProducts();

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    boolean existsByBarcode(String barcode);
}
