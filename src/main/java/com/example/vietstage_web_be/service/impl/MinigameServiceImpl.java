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

        // Note: As specified, "Cộng điểm; check achievements/cosmetics; leaderboard cập nhật trong Redis"
        // The points logic can be further elaborated, for now, we will assign points based on starsEarned as a stub.
        Integer pointsEarned = request.getStarsEarned() * 5; // e.g., 5 points per star

        MinigameAttempt attempt = MinigameAttempt.builder()
                .challenge(challenge)
                .learner(learner)
                .score(request.getScore())
                .starsEarned(request.getStarsEarned())
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .build();

        attempt = attemptRepository.save(attempt);
        
        leaderboardService.addPoints(learner, pointsEarned, "MINI_GAME");

        return MinigameAttemptResponse.builder()
                .id(attempt.getId())
                .minigameId(challenge.getId())
                .learnerId(learner.getId())
                .score(attempt.getScore())
                .starsEarned(attempt.getStarsEarned())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .pointsEarned(pointsEarned)
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
