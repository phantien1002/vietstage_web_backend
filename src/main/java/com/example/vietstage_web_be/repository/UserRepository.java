package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"role"})
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findTopByOrderByIdDesc();

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:role IS NULL OR :role = '' OR u.role.name = :role)")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"role"})
    org.springframework.data.domain.Page<User> searchUsers(
        @org.springframework.data.repository.query.Param("search") String search, 
        @org.springframework.data.repository.query.Param("role") String role, 
        org.springframework.data.domain.Pageable pageable
    );
}