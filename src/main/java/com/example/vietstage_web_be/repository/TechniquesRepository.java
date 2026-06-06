package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Techniques;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechniquesRepository extends JpaRepository<Techniques, Long> {
    List<Techniques> findByInstrumentId(Long instrumentId);
    boolean existsByNameIgnoreCaseAndInstrumentId(String name, Long instrumentId);
}
