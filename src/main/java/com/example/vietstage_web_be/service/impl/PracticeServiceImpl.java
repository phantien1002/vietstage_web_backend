package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.BulkPracticeAttemptRequest;
import com.example.vietstage_web_be.dto.request.EndSessionRequest;
import com.example.vietstage_web_be.dto.request.PracticeAttemptRequest;
import com.example.vietstage_web_be.dto.response.BulkPracticeAttemptResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.PracticeAttemptResponse;
import com.example.vietstage_web_be.dto.response.PracticeSessionResponse;
import com.example.vietstage_web_be.entity.*;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.*;
import com.example.vietstage_web_be.service.ILeaderboardService;
import com.example.vietstage_web_be.service.ILearnerProgressService;
import com.example.vietstage_web_be.service.IPracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PracticeServiceImpl implements IPracticeService {

    private final PracticeSessionRepository sessionRepository;
    private final PracticeAttemptRepository attemptRepository;
    private final ExerciseRepository exerciseRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final ILeaderboardService leaderboardService;
    private final ILearnerProgressService progressService;
    private final LessonRepository lessonRepository;

    @Override
    @Transactional
    public PracticeSessionResponse startSession(User learner) {
        PracticeSession session = PracticeSession.builder()
                .learner(learner)
                .startedAt(LocalDateTime.now())
                .build();
        sessionRepository.save(session);
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public PracticeSessionResponse endSession(User learner, Long sessionId, EndSessionRequest request) {
        PracticeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!session.getLearner().getId().equals(learner.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN); // Or create specific error
        }

        session.setEndedAt(request.getEndedAt());
        Duration duration = Duration.between(session.getStartedAt(), session.getEndedAt());
        int minutes = (int) duration.toMinutes();
        session.setDurationMinutes(minutes);
        sessionRepository.save(session);

        // Update learner profile practice seconds
        LearnerProfile profile = learnerProfileRepository.findById(learner.getId())
                .orElseGet(() -> LearnerProfile.builder().userId(learner.getId()).user(learner).build());
        
        long currentSeconds = profile.getTotalPracticeSeconds() != null ? profile.getTotalPracticeSeconds() : 0L;
        profile.setTotalPracticeSeconds(currentSeconds + duration.getSeconds());
        progressService.updateStreakAndSave(profile);

        return mapSessionToResponse(session);
    }

    @Override
    public PageResponse<PracticeSessionResponse> getHistorySessions(User learner, Pageable pageable) {
        Page<PracticeSession> page = sessionRepository.findByLearnerId(learner.getId(), pageable);
        List<PracticeSessionResponse> responses = page.getContent().stream()
                .map(this::mapSessionToResponse).toList();

        return PageResponse.<PracticeSessionResponse>builder()
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .content(responses)
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public PracticeAttemptResponse submitAttempt(User learner, PracticeAttemptRequest request) {
        return processSingleAttempt(learner, request, false);
    }

    @Override
    public PageResponse<PracticeAttempt> getMyAttempts(User learner, Long exerciseId, Pageable pageable) {
        Page<PracticeAttempt> page;
        if (exerciseId != null) {
            page = attemptRepository.findByLearnerIdAndExerciseId(learner.getId(), exerciseId, pageable);
        } else {
            page = attemptRepository.findByLearnerId(learner.getId(), pageable);
        }
        
        return PageResponse.<PracticeAttempt>builder()
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .content(page.getContent())
                .last(page.isLast())
                .build();
    }

    @Override
    public PracticeAttempt getAttemptDetails(User user, Long attemptId) {
        PracticeAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (user.getRole().getName().equals("INSTRUCTOR")) {
            if (!attempt.getExercise().getLesson().getCreatedBy().getId().equals(user.getId())) {
                throw new AppException(ErrorCode.FORBIDDEN);
            }
        } else if (user.getRole().getName().equals("LEARNER")) {
            if (!attempt.getLearner().getId().equals(user.getId())) {
                throw new AppException(ErrorCode.FORBIDDEN);
            }
        } else {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        return attempt;
    }

    @Override
    public PageResponse<PracticeAttempt> getLearnerAttemptsForLesson(User instructor, Long lessonId, Long learnerId, Pageable pageable) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!lesson.getCreatedBy().getId().equals(instructor.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Page<PracticeAttempt> page = attemptRepository.findByExerciseLessonIdAndLearnerId(lessonId, learnerId, pageable);
        return PageResponse.<PracticeAttempt>builder()
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .content(page.getContent())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public BulkPracticeAttemptResponse submitBulkAttempts(User learner, BulkPracticeAttemptRequest request) {
        int createdCount = 0;
        List<BulkPracticeAttemptResponse.Conflict> conflicts = new ArrayList<>();

        for (PracticeAttemptRequest attemptRequest : request.getAttempts()) {
            if (attemptRequest.getClientUuid() != null) {
                Optional<PracticeAttempt> existing = attemptRepository.findByClientUuid(attemptRequest.getClientUuid());
                if (existing.isPresent()) {
                    conflicts.add(BulkPracticeAttemptResponse.Conflict.builder()
                            .clientUuid(attemptRequest.getClientUuid())
                            .reason("Attempt already synced")
                            .build());
                    continue;
                }
            }
            try {
                processSingleAttempt(learner, attemptRequest, true);
                createdCount++;
            } catch (Exception e) {
                conflicts.add(BulkPracticeAttemptResponse.Conflict.builder()
                        .clientUuid(attemptRequest.getClientUuid())
                        .reason(e.getMessage())
                        .build());
            }
        }

        return BulkPracticeAttemptResponse.builder()
                .created(createdCount)
                .conflicts(conflicts)
                .build();
    }

    private PracticeAttemptResponse processSingleAttempt(User learner, PracticeAttemptRequest request, boolean isBulk) {
        PracticeSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        BigDecimal pitch = request.getPitchScore() != null ? request.getPitchScore() : BigDecimal.ZERO;
        BigDecimal rhythm = request.getRhythmScore() != null ? request.getRhythmScore() : BigDecimal.ZERO;
        BigDecimal dyn = request.getDynamicsScore() != null ? request.getDynamicsScore() : BigDecimal.ZERO;
        BigDecimal tonal = request.getTonalQualityScore() != null ? request.getTonalQualityScore() : BigDecimal.ZERO;
        BigDecimal breath = request.getBreathScore() != null ? request.getBreathScore() : BigDecimal.ZERO;

        BigDecimal total = pitch.add(rhythm).add(dyn).add(tonal).add(breath).divide(new BigDecimal("5"), 2, RoundingMode.HALF_UP);
        
        int stars = 0;
        if (total.compareTo(new BigDecimal("90")) >= 0) stars = 3;
        else if (total.compareTo(new BigDecimal("70")) >= 0) stars = 2;
        else if (total.compareTo(new BigDecimal("50")) >= 0) stars = 1;

        int points = stars * 10; // Simple formula

        PracticeAttempt attempt = PracticeAttempt.builder()
                .learner(learner)
                .practiceSession(session)
                .exercise(exercise)
                .pitchScore(pitch)
                .rhythmScore(rhythm)
                .dynamicsScore(dyn)
                .tonalQualityScore(tonal)
                .breathScore(breath)
                .totalScore(total)
                .stars(stars)
                .pointsEarned(points)
                .clientUuid(request.getClientUuid())
                .syncStatus("SYNCED")
                .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now())
                .build();
        
        attemptRepository.save(attempt);
        leaderboardService.addPoints(learner, points, "PRACTICE");

        return PracticeAttemptResponse.builder()
                .id(attempt.getId())
                .totalScore(total)
                .stars(stars)
                .pointsEarned(points)
                .build();
    }

    private PracticeSessionResponse mapSessionToResponse(PracticeSession session) {
        return PracticeSessionResponse.builder()
                .id(session.getId())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationMinutes(session.getDurationMinutes())
                .build();
    }
}
