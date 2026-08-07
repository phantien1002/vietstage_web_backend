package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Lesson;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LessonSpecification {

    public static Specification<Lesson> filterBy(String status, Long instructorId, Long instrumentId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("status")), 
                        status.toUpperCase()
                ));
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
