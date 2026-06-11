package com.example.vietstage_web_be.specification;

import com.example.vietstage_web_be.entity.Lessons;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LessonSpecification {

    public static Specification<Lessons> filter(String search, Long instrumentId, Long skillLevelId, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), pattern));
            }

            if (instrumentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("instrument").get("id"), instrumentId));
            }

            // v2.0: filter by skillLevelId (FK) instead of difficulty string
            if (skillLevelId != null) {
                predicates.add(criteriaBuilder.equal(root.get("skillLevel").get("id"), skillLevelId));
            }

            // Filter by status: DRAFT | PENDING | APPROVED | REJECTED
            if (status != null && !status.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("status")), status.toUpperCase()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
