package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Lessons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonsRepository extends JpaRepository<Lessons, Long>, JpaSpecificationExecutor<Lessons> {
}
