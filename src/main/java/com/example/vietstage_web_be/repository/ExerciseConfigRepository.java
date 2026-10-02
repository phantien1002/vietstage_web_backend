package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.ExerciseConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseConfigRepository extends JpaRepository<ExerciseConfig, String> {
    ExerciseConfig findByActivityCode(String activityCode);
}
