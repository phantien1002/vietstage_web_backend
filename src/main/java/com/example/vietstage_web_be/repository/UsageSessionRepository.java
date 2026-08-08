package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.UsageSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsageSessionRepository extends JpaRepository<UsageSession, UUID> {
}
