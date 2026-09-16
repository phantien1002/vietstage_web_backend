package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.CompletionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletionAttemptRepository extends JpaRepository<CompletionAttempt, CompletionAttempt.CompletionAttemptId> {
}
