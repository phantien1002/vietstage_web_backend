package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LearnerCosmetic;
import com.example.vietstage_web_be.entity.LearnerCosmeticsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearnerCosmeticRepository extends JpaRepository<LearnerCosmetic, LearnerCosmeticsId> {
    List<LearnerCosmetic> findByLearnerId(Long learnerId);
}