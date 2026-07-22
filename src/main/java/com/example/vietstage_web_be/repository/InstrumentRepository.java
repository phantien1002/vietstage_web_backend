package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
    boolean existsByNameIgnoreCase(String name);
    java.util.Optional<Instrument> findTopByOrderByIdDesc();
}
