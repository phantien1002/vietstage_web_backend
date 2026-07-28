package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.dto.auth.AuthSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthSessionRepository extends CrudRepository<AuthSession, String> {
}
