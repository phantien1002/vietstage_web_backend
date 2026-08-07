package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.ReviewItemResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAdminReviewService {
    PageResponse<ReviewItemResponse> getAllReviews(String status, Long instructorId, Long instrumentId, Pageable pageable);
    void approveReview(Long id, Long adminId);
    void rejectReview(Long id, String feedback, Long adminId);
}
