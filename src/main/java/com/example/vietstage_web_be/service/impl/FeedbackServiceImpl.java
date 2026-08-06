package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.FeedbackRequest;
import com.example.vietstage_web_be.dto.response.FeedbackResponse;
import com.example.vietstage_web_be.entity.InstructorFeedback;
import com.example.vietstage_web_be.entity.PracticeAttempt;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.InstructorFeedbackRepository;
import com.example.vietstage_web_be.repository.PracticeAttemptRepository;
import com.example.vietstage_web_be.service.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements IFeedbackService {

    private final InstructorFeedbackRepository feedbackRepository;
    private final PracticeAttemptRepository attemptRepository;

    @Override
    @Transactional
    public FeedbackResponse submitFeedback(User instructor, Long attemptId, FeedbackRequest request) {
        PracticeAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!attempt.getExercise().getLesson().getCreatedBy().getId().equals(instructor.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN); // Or create specific error for instructor feedback
        }

        Optional<InstructorFeedback> existingFeedback = feedbackRepository.findByPracticeAttemptIdAndInstructorId(attemptId, instructor.getId());
        if (existingFeedback.isPresent()) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Already given feedback
        }

        InstructorFeedback feedback = InstructorFeedback.builder()
                .instructor(instructor)
                .practiceAttempt(attempt)
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(feedback);

        return FeedbackResponse.builder()
                .id(feedback.getId())
                .instructorName(instructor.getFullName())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    @Override
    public List<FeedbackResponse> getFeedbackForAttempt(User currentUser, Long attemptId) {
        PracticeAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (currentUser.getRole().getName().equals("INSTRUCTOR")) {
            if (!attempt.getExercise().getLesson().getCreatedBy().getId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.FORBIDDEN);
            }
        } else if (currentUser.getRole().getName().equals("LEARNER")) {
            if (!attempt.getLearner().getId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.FORBIDDEN);
            }
        } else {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        return feedbackRepository.findByPracticeAttemptId(attemptId).stream()
                .map(f -> FeedbackResponse.builder()
                        .id(f.getId())
                        .instructorName(f.getInstructor().getFullName())
                        .comment(f.getComment())
                        .createdAt(f.getCreatedAt())
                        .build())
                .toList();
    }
}
