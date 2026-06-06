package com.example.vietstage_web_be.specification;

import com.example.vietstage_web_be.entity.Lessons;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LessonSpecification {

    /**
     * Tìm kiếm bài học theo tiêu đề, lọc theo nhạc cụ và độ khó.
     */
    public static Specification<Lessons> filter(String search, Long instrumentId, String difficulty) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm kiếm mờ theo title
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), pattern));
            }

            // Lọc theo instrumentId
            if (instrumentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("instrument").get("id"), instrumentId));
            }

            // Lọc theo difficulty (chấp nhận không phân biệt hoa thường)
            if (difficulty != null && !difficulty.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("difficulty")), difficulty.toLowerCase()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
