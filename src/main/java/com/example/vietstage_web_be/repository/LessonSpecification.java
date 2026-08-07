package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Lesson;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LessonSpecification {

    public static Specification<Lesson> filterBy(String status, String search, Long instructorId, Long instrumentId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("status")), 
                        status.toUpperCase()
                ));
            }

            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate instructorPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.join("createdBy").get("fullName")), searchPattern);
                predicates.add(criteriaBuilder.or(titlePredicate, instructorPredicate));
            }

            if (instructorId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("createdBy").get("id"), 
                        instructorId
                ));
            }

            if (instrumentId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("instrument").get("id"), 
                        instrumentId
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
