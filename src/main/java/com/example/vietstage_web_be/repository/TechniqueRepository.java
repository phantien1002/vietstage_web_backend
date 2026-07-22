package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Technique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechniqueRepository extends JpaRepository<Technique, Long> {
    List<Technique> findByInstrumentId(Long instrumentId);
    boolean existsByNameIgnoreCaseAndInstrumentId(String name, Long instrumentId);
}
