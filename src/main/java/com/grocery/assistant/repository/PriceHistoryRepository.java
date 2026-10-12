package com.grocery.assistant.repository;

import com.grocery.assistant.entity.PriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByProductIdOrderByCreatedAtDesc(Long productId);
    Page<PriceHistory> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    Page<PriceHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
