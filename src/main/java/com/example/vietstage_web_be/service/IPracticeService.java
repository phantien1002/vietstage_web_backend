package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.BulkPracticeAttemptRequest;
import com.example.vietstage_web_be.dto.request.EndSessionRequest;
import com.example.vietstage_web_be.dto.request.PracticeAttemptRequest;
import com.example.vietstage_web_be.dto.response.BulkPracticeAttemptResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.PracticeAttemptResponse;
import com.example.vietstage_web_be.dto.response.PracticeSessionResponse;
import com.example.vietstage_web_be.entity.PracticeAttempt;
import com.example.vietstage_web_be.entity.User;
import org.springframework.data.domain.Pageable;

public interface IPracticeService {
    PracticeSessionResponse startSession(User learner);
    PracticeSessionResponse endSession(User learner, Long sessionId, EndSessionRequest request);
    PageResponse<PracticeSessionResponse> getHistorySessions(User learner, Pageable pageable);
    
    PracticeAttemptResponse submitAttempt(User learner, PracticeAttemptRequest request);
    PageResponse<PracticeAttempt> getMyAttempts(User learner, Long exerciseId, Pageable pageable);
    PracticeAttempt getAttemptDetails(Long attemptId);
    PageResponse<PracticeAttempt> getLearnerAttemptsForLesson(User instructor, Long lessonId, Long learnerId, Pageable pageable);
    
    BulkPracticeAttemptResponse submitBulkAttempts(User learner, BulkPracticeAttemptRequest request);
}