package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LearnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearnerProfileRepository extends JpaRepository<LearnerProfile, Long> {
    java.util.Optional<LearnerProfile> findByUserId(Long userId);
}