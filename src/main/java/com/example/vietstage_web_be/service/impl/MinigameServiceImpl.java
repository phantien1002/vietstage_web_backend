package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.MinigameAttemptRequest;
import com.example.vietstage_web_be.dto.request.MinigameChallengeRequest;
import com.example.vietstage_web_be.dto.response.MinigameAttemptResponse;
import com.example.vietstage_web_be.dto.response.MinigameChallengeResponse;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.MinigameAttempt;
import com.example.vietstage_web_be.entity.MinigameChallenge;
import com.example.vietstage_web_be.entity.PointTransaction;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.AppConfigRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.MediaAssetRepository;
import com.example.vietstage_web_be.repository.MinigameAttemptRepository;
import com.example.vietstage_web_be.repository.MinigameChallengeRepository;
import com.example.vietstage_web_be.repository.PointTransactionRepository;
import com.example.vietstage_web_be.service.IMinigameService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MinigameServiceImpl implements IMinigameService {

    private final MinigameChallengeRepository challengeRepository;
    private final MinigameAttemptRepository attemptRepository;
    private final LessonRepository lessonRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final AppConfigRepository appConfigRepository;
    private final PointTransactionRepository pointTransactionRepository;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<MinigameChallengeResponse> getMinigamesByLesson(Long lessonId, User currentUser) {
        List<MinigameChallenge> challenges = challengeRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        return challenges.stream().map(c -> mapToResponse(c, currentUser)).collect(Collectors.toList());
    }

    @Override
    public MinigameChallengeResponse createMinigame(Long lessonId, MinigameChallengeRequest request) {
        validateChallengeType(request.getChallengeType());
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (request.getReferenceAssetId() != null) {
            mediaAssetRepository.findById(request.getReferenceAssetId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        MinigameChallenge challenge = MinigameChallenge.builder()
                .lesson(lesson)
                .title(request.getTitle())
                .challengeType(request.getChallengeType())
                .contentJson(request.getContentJson())
                .referenceAssetId(request.getReferenceAssetId())
                .difficulty(request.getDifficulty())
                .maxScore(request.getMaxScore())
                .orderIndex(request.getOrderIndex())
                .createdAt(LocalDateTime.now())
                .build();

        challenge = challengeRepository.save(challenge);
        return mapToResponse(challenge, null);
    }

    @Override
    public MinigameChallengeResponse updateMinigame(Long id, MinigameChallengeRequest request) {
        validateChallengeType(request.getChallengeType());
        
        MinigameChallenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MINIGAME_NOT_FOUND));

        if (request.getReferenceAssetId() != null) {
            mediaAssetRepository.findById(request.getReferenceAssetId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        challenge.setTitle(request.getTitle());
        challenge.setChallengeType(request.getChallengeType());
        challenge.setContentJson(request.getContentJson());
        challenge.setReferenceAssetId(request.getReferenceAssetId());
        challenge.setDifficulty(request.getDifficulty());
        challenge.setMaxScore(request.getMaxScore());
        challenge.setOrderIndex(request.getOrderIndex());

        challenge = challengeRepository.save(challenge);
        return mapToResponse(challenge, null);
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

        Integer score = request.getScore();
        Integer starsEarned = request.getStarsEarned();
        Map<String, Boolean> answerResults = null;
        Map<String, String> correctAnswersMap = null;

        if ("MELODY_COMPLETE".equals(challenge.getChallengeType())) {
            // Server-side scoring
            try {
                JsonNode root = objectMapper.readTree(challenge.getContentJson());
                JsonNode correctAnswersNode = root.get("correct_answers");
                
                if (correctAnswersNode != null && correctAnswersNode.isObject()) {
                    correctAnswersMap = objectMapper.convertValue(correctAnswersNode, new TypeReference<Map<String, String>>() {});
                    answerResults = new HashMap<>();
                    
                    int totalAnswers = correctAnswersMap.size();
                    int correctCount = 0;
                    
                    Map<String, String> learnerAnswers = request.getAnswers();
                    if (learnerAnswers == null) learnerAnswers = new HashMap<>();
                    
                    for (Map.Entry<String, String> entry : correctAnswersMap.entrySet()) {
                        String position = entry.getKey();
                        String correctNote = entry.getValue();
                        String learnerNote = learnerAnswers.get(position);
                        
                        boolean isCorrect = correctNote != null && correctNote.equals(learnerNote);
                        answerResults.put(position, isCorrect);
                        if (isCorrect) correctCount++;
                    }
                    
                    if (totalAnswers > 0) {
                        double percentage = (double) correctCount / totalAnswers;
                        score = (int) Math.round(percentage * challenge.getMaxScore());
                    } else {
                        score = 0;
                    }
                    
                    // Determine stars from configs
                    int star1 = getAppConfigInt("scoring.star1.threshold", 50);
                    int star2 = getAppConfigInt("scoring.star2.threshold", 70);
                    int star3 = getAppConfigInt("scoring.star3.threshold", 90);
                    
                    if (score >= star3) starsEarned = 3;
                    else if (score >= star2) starsEarned = 2;
                    else if (score >= star1) starsEarned = 1;
                    else starsEarned = 0;
                }
            } catch (Exception e) {
                // Fallback if parsing fails
                score = 0;
                starsEarned = 0;
            }
        }

        int multiplier = getAppConfigInt("scoring.points.multiplier", 5);
        Integer pointsEarned = starsEarned * multiplier;

        MinigameAttempt attempt = MinigameAttempt.builder()
                .challenge(challenge)
                .learner(learner)
                .score(score)
                .starsEarned(starsEarned)
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .build();

        attempt = attemptRepository.save(attempt);
        
        if (pointsEarned > 0) {
            PointTransaction transaction = PointTransaction.builder()
                    .user(learner)
                    .sourceType("MINI_GAME")
                    .points(pointsEarned)
                    .build();
            pointTransactionRepository.save(transaction);
        }

        return MinigameAttemptResponse.builder()
                .id(attempt.getId())
                .minigameId(challenge.getId())
                .learnerId(learner.getId())
                .score(score)
                .starsEarned(starsEarned)
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .pointsEarned(pointsEarned)
                .answerResults(answerResults)
                .correctAnswers(correctAnswersMap)
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
                // Historic points fetched/computed
                .pointsEarned(attempt.getStarsEarned() * getAppConfigInt("scoring.points.multiplier", 5))
                .build());
    }
    
    private MinigameChallengeResponse mapToResponse(MinigameChallenge challenge, User currentUser) {
        String contentJson = challenge.getContentJson();
        
        if ("MELODY_COMPLETE".equals(challenge.getChallengeType()) && contentJson != null) {
            try {
                ObjectNode root = (ObjectNode) objectMapper.readTree(contentJson);
                
                // Inject real audio URL
                if (root.has("audio_asset_id")) {
                    Long audioAssetId = root.get("audio_asset_id").asLong();
                    mediaAssetRepository.findById(audioAssetId).ifPresent(asset -> {
                        root.put("audio_url", asset.getAssetUrl());
                    });
                }
                
                // Mask answers for learners
                if (currentUser != null && "LEARNER".equals(currentUser.getRole().getName())) {
                    root.remove("correct_answers");
                }
                
                contentJson = objectMapper.writeValueAsString(root);
            } catch (Exception e) {
                // ignore parsing error
            }
        }
        
        return MinigameChallengeResponse.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .challengeType(challenge.getChallengeType())
                .difficulty(challenge.getDifficulty())
                .maxScore(challenge.getMaxScore())
                .orderIndex(challenge.getOrderIndex())
                .referenceAssetId(challenge.getReferenceAssetId())
                .contentJson(contentJson)
                .build();
    }
    
    private void validateChallengeType(String type) {
        if (!"RHYTHM_MATCH".equals(type) && !"MELODY_COMPLETE".equals(type)) {
            throw new AppException(ErrorCode.MINIGAME_INVALID_TYPE);
        }
    }
    
    private int getAppConfigInt(String key, int defaultValue) {
        return appConfigRepository.findByConfigKey(key)
                .map(config -> {
                    try {
                        // value is either JSON string or raw string
                        String val = config.getConfigValue();
                        if (val.startsWith("\"") && val.endsWith("\"")) {
                            val = val.substring(1, val.length() - 1);
                        }
                        return Integer.parseInt(val);
                    } catch (Exception e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }
}
