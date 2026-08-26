package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.MinigameAttemptRequest;
import com.example.vietstage_web_be.dto.request.MinigameChallengeRequest;
import com.example.vietstage_web_be.dto.response.MinigameAttemptResponse;
import com.example.vietstage_web_be.dto.response.MinigameChallengeResponse;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.MinigameAttempt;
import com.example.vietstage_web_be.entity.MinigameChallenge;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.MinigameAttemptRepository;
import com.example.vietstage_web_be.repository.MinigameChallengeRepository;
import com.example.vietstage_web_be.service.IMinigameService;
import com.example.vietstage_web_be.service.ILeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MinigameServiceImpl implements IMinigameService {

    private final MinigameChallengeRepository challengeRepository;
    private final MinigameAttemptRepository attemptRepository;
    private final LessonRepository lessonRepository;
    private final ILeaderboardService leaderboardService;

    @Override
    public List<MinigameChallengeResponse> getMinigamesByLesson(Long lessonId) {
        List<MinigameChallenge> challenges = challengeRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        return challenges.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public MinigameChallengeResponse createMinigame(Long lessonId, MinigameChallengeRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        MinigameChallenge challenge = MinigameChallenge.builder()
                .lesson(lesson)
                .title(request.getTitle())
                .challengeType(request.getChallengeType())
                .contentJson(request.getContentJson())
                .difficulty(request.getDifficulty())
                .maxScore(request.getMaxScore())
                .orderIndex(request.getOrderIndex())
                .createdAt(LocalDateTime.now())
                .build();

        challenge = challengeRepository.save(challenge);
        return mapToResponse(challenge);
    }

    @Override
    public MinigameChallengeResponse updateMinigame(Long id, MinigameChallengeRequest request) {
        MinigameChallenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MINIGAME_NOT_FOUND));

        challenge.setTitle(request.getTitle());
        challenge.setChallengeType(request.getChallengeType());
        challenge.setContentJson(request.getContentJson());
        challenge.setDifficulty(request.getDifficulty());
        challenge.setMaxScore(request.getMaxScore());
        challenge.setOrderIndex(request.getOrderIndex());

        challenge = challengeRepository.save(challenge);
        return mapToResponse(challenge);
    }

    @Override
    public void deleteMinigame(Long id) {
        if (!challengeRepository.existsById(id)) {
            throw new AppException(ErrorCode.MINIGAME_NOT_FOUND);
        }
        challengeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public MinigameAttemptResponse submitAttempt(Long minigameId, MinigameAttemptRequest request, User learner) {
        MinigameChallenge challenge = challengeRepository.findById(minigameId)
                .orElseThrow(() -> new AppException(ErrorCode.MINIGAME_NOT_FOUND));

        com.example.vietstage_web_be.entity.LearnerProfile profile = com.example.vietstage_web_be.repository.LearnerProfileRepository.class.cast(org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() != null ? ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest().getServletContext() : null).getBean(com.example.vietstage_web_be.repository.LearnerProfileRepository.class)).findByUserId(learner.getId()).orElse(null);

        if (request.getClientAttemptId() != null) {
            java.util.Optional<MinigameAttempt> existingAttempt = attemptRepository.findByClientAttemptId(request.getClientAttemptId());
            if (existingAttempt.isPresent()) {
                MinigameAttempt attempt = existingAttempt.get();
                return MinigameAttemptResponse.builder()
                        .id(attempt.getId())
                        .minigameId(challenge.getId())
                        .learnerId(learner.getId())
                        .score(attempt.getScore())
                        .starsEarned(0) // Return 0 earned for retry
                        .totalStars(profile != null ? profile.getTotalStars() : 0)
                        .spendableStars(profile != null ? profile.getSpendableStars() : 0)
                        .totalPoints(profile != null ? profile.getTotalPoints() : 0)
                        .startedAt(attempt.getStartedAt())
                        .completedAt(attempt.getCompletedAt())
                        .pointsEarned(attempt.getStarsEarned() * 5)
                        .build();
            }
        }

        // Calculate stars based on score (e.g. maxScore == 3 stars)
        int starsEarned = 0;
        if (challenge.getMaxScore() != null && challenge.getMaxScore() > 0 && request.getScore() != null) {
            double ratio = (double) request.getScore() / challenge.getMaxScore();
            if (ratio >= 0.9) starsEarned = 3;
            else if (ratio >= 0.7) starsEarned = 2;
            else if (ratio >= 0.5) starsEarned = 1;
        }

        Integer pointsEarned = starsEarned * 5;

        MinigameAttempt attempt = MinigameAttempt.builder()
                .challenge(challenge)
                .learner(learner)
                .score(request.getScore())
                .starsEarned(starsEarned)
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .clientAttemptId(request.getClientAttemptId())
                .build();

        attempt = attemptRepository.save(attempt);
        
        if (pointsEarned > 0) {
            leaderboardService.addPoints(learner, pointsEarned, "MINI_GAME");
        }
        
        if (profile != null && starsEarned > 0) {
            profile.setTotalStars(profile.getTotalStars() + starsEarned);
            profile.setSpendableStars(profile.getSpendableStars() + starsEarned);
            com.example.vietstage_web_be.repository.LearnerProfileRepository.class.cast(org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() != null ? ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest().getServletContext() : null).getBean(com.example.vietstage_web_be.repository.LearnerProfileRepository.class)).save(profile);
        }

        return MinigameAttemptResponse.builder()
                .id(attempt.getId())
                .minigameId(challenge.getId())
                .learnerId(learner.getId())
                .score(attempt.getScore())
                .starsEarned(starsEarned)
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .pointsEarned(pointsEarned)
                .totalStars(profile != null ? profile.getTotalStars() : 0)
                .spendableStars(profile != null ? profile.getSpendableStars() : 0)
                .totalPoints(profile != null ? profile.getTotalPoints() : 0)
                .build();
    }

    @Override
    public Page<MinigameAttemptResponse> getAttempts(Long minigameId, Pageable pageable, User learner) {
        Page<MinigameAttempt> attempts = attemptRepository.findByChallengeIdAndLearnerIdOrderByCompletedAtDesc(minigameId, learner.getId(), pageable);
        return attempts.map(attempt -> MinigameAttemptResponse.builder()
                .id(attempt.getId())
                .minigameId(attempt.getChallenge().getId())
                .learnerId(attempt.getLearner().getId())
                .score(attempt.getScore())
                .starsEarned(attempt.getStarsEarned())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .pointsEarned(attempt.getStarsEarned() * 5)
                .build());
    }
    
    private MinigameChallengeResponse mapToResponse(MinigameChallenge challenge) {
        return MinigameChallengeResponse.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .challengeType(challenge.getChallengeType())
                .difficulty(challenge.getDifficulty())
                .maxScore(challenge.getMaxScore())
                .orderIndex(challenge.getOrderIndex())
                .contentJson(challenge.getContentJson())
                .build();
    }
}
