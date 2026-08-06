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
           "(COALESCE(:roles, NULL) IS NULL OR u.role.name IN :roles) AND " +
           "(:isActive IS NULL OR u.active = :isActive)")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"role"})
    org.springframework.data.domain.Page<User> searchUsers(
        @org.springframework.data.repository.query.Param("search") String search, 
        @org.springframework.data.repository.query.Param("roles") java.util.List<String> roles, 
        @org.springframework.data.repository.query.Param("isActive") Boolean isActive,
        org.springframework.data.domain.Pageable pageable
    );

    long countByRoleName(String roleName);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT lc.learner) FROM LessonCompletion lc WHERE lc.lesson.createdBy.id = :instructorId AND lc.learner.role.name = 'LEARNER'")
    long countLearnersForInstructor(@org.springframework.data.repository.query.Param("instructorId") Long instructorId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT lc.learner FROM LessonCompletion lc WHERE " +
           "lc.lesson.createdBy.id = :instructorId AND lc.learner.role.name = 'LEARNER' AND " +
           "(:search IS NULL OR :search = '' OR LOWER(lc.learner.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(lc.learner.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"role", "learnerProfile"})
    org.springframework.data.domain.Page<User> findLearnersForInstructor(
        @org.springframework.data.repository.query.Param("instructorId") Long instructorId, 
        @org.springframework.data.repository.query.Param("search") String search, 
        org.springframework.data.domain.Pageable pageable
    );
}