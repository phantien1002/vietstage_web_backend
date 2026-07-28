package com.example.vietstage_web_be.specification;

import com.example.vietstage_web_be.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    /**
     * Tìm kiếm theo keyword (email hoặc fullName), lọc theo role và trạng thái active.
     *
     * @param keyword  từ khóa tìm kiếm theo email hoặc fullName (nullable)
     * @param role     lọc theo role: ADMIN, INSTRUCTOR, LEARNER (nullable)
     * @param isActive lọc theo trạng thái active (nullable)
     */
    public static Specification<User> filter(String keyword, String role, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm kiếm theo email hoặc fullName (LIKE, case-insensitive)
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                Predicate emailLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), pattern);
                Predicate nameLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")), pattern);
                predicates.add(criteriaBuilder.or(emailLike, nameLike));
            }

            // Lọc theo role
            if (role != null && !role.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("role")), role.toUpperCase()));
            }

            // Lọc theo trạng thái active
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), isActive));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
