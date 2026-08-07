package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.PracticeSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {
    Page<PracticeSession> findByLearnerId(Long learnerId, Pageable pageable);

    @Query("SELECT ps FROM PracticeSession ps WHERE " +
           "(:fromDate IS NULL OR ps.startedAt >= :fromDate) AND " +
           "(:toDate IS NULL OR ps.startedAt <= :toDate)")
    List<PracticeSession> findAllByDateRange(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}