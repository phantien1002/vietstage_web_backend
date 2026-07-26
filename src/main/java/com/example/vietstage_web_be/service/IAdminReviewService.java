package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.ReviewItemResponse;
import java.util.List;

public interface IAdminReviewService {
    List<ReviewItemResponse> getAllReviews();
    void approveReview(Long id, Long adminId);
    void rejectReview(Long id, String feedback, Long adminId);
}
