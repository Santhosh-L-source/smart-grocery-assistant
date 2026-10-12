package com.grocery.assistant.repository;

import com.grocery.assistant.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    Optional<Sale> findBySaleNumber(String saleNumber);
    boolean existsBySaleNumber(String saleNumber);
    Page<Sale> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
