package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.UserSessions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserSessionsRepository extends JpaRepository<UserSessions, Long> {

}