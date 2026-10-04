package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LearnerCosmetic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearnerCosmeticRepository extends JpaRepository<LearnerCosmetic, Long> {
    List<LearnerCosmetic> findByLearnerId(Long learnerId);
    long countByCosmeticItemId(Long cosmeticItemId);
    Optional<LearnerCosmetic> findByLearnerIdAndCosmeticItemId(Long learnerId, Long cosmeticItemId);
}