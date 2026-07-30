package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    Page<PointTransaction> findByUserId(Long userId, Pageable pageable);
    Page<PointTransaction> findByUserIdAndSourceType(Long userId, String sourceType, Pageable pageable);
}