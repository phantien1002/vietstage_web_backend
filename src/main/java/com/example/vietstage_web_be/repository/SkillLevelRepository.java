package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillLevelRepository extends JpaRepository<SkillLevel, Long> {
}
