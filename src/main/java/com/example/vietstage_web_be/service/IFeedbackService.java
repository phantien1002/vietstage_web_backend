package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.FeedbackRequest;
import com.example.vietstage_web_be.dto.response.FeedbackResponse;
import com.example.vietstage_web_be.entity.User;

import java.util.List;

public interface IFeedbackService {
    FeedbackResponse submitFeedback(User instructor, Long attemptId, FeedbackRequest request);
    List<FeedbackResponse> getFeedbackForAttempt(Long attemptId);
}