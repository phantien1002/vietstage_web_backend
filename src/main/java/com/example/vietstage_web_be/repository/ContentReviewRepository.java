package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.ContentReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentReviewRepository extends JpaRepository<ContentReview, Long> {
}
