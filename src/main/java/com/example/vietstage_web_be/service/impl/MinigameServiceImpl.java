package com.example.vietstage_web_be.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.vietstage_web_be.dto.request.MinigameAttemptRequest;
import com.example.vietstage_web_be.dto.request.MinigameChallengeRequest;
import com.example.vietstage_web_be.dto.response.MinigameAttemptResponse;
import com.example.vietstage_web_be.dto.response.MinigameChallengeResponse;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.MinigameAttempt;
import com.example.vietstage_web_be.entity.MinigameChallenge;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.entity.MediaAsset;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.MinigameAttemptRepository;
import com.example.vietstage_web_be.repository.MinigameChallengeRepository;
import com.example.vietstage_web_be.repository.MediaAssetRepository;
import com.example.vietstage_web_be.service.IMinigameService;
import com.example.vietstage_web_be.service.ILeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MinigameServiceImpl implements IMinigameService {

    private final MinigameChallengeRepository challengeRepository;
    private final MinigameAttemptRepository attemptRepository;
    private final LessonRepository lessonRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final ILeaderboardService leaderboardService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<MinigameChallengeResponse> getMinigamesByLesson(Long lessonId) {
        List<MinigameChallenge> challenges = challengeRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        return challenges.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MinigameChallengeResponse createMinigame(User actor, Long lessonId, MinigameChallengeRequest request) {
        validateMinigameRequest(request);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        validateLessonOwnership(actor, lesson);

        MinigameChallenge challenge = MinigameChallenge.builder()
                .lesson(lesson)
                .title(request.getTitle())
                .challengeType(request.getChallengeType())
                .contentJson(prepareContentJson(lesson, request))
                .difficulty(request.getDifficulty())
                .maxScore(request.getMaxScore())
                .orderIndex(request.getOrderIndex())
                .createdAt(LocalDateTime.now())
                .build();

        challenge = challengeRepository.save(challenge);
        return mapToResponse(challenge);
    }

    @Override
    @Transactional
    public MinigameChallengeResponse updateMinigame(User actor, Long id, MinigameChallengeRequest request) {
        validateMinigameRequest(request);
        MinigameChallenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MINIGAME_NOT_FOUND));
        validateLessonOwnership(actor, challenge.getLesson());

        challenge.setTitle(request.getTitle());
        challenge.setChallengeType(request.getChallengeType());
        challenge.setContentJson(prepareContentJson(challenge.getLesson(), request));
        challenge.setDifficulty(request.getDifficulty());
        challenge.setMaxScore(request.getMaxScore());
        challenge.setOrderIndex(request.getOrderIndex());

        challenge = challengeRepository.save(challenge);
        return mapToResponse(challenge);
    }

    @Override
    @Transactional
    public void deleteMinigame(User actor, Long id) {
        MinigameChallenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MINIGAME_NOT_FOUND));
        validateLessonOwnership(actor, challenge.getLesson());
        challengeRepository.delete(challenge);
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

    private void validateLessonOwnership(User actor, Lesson lesson) {
        if (actor != null && actor.getRole() != null && "ADMIN".equalsIgnoreCase(actor.getRole().getName())) {
            return;
        }
        if (actor == null || lesson.getCreatedBy() == null || !actor.getId().equals(lesson.getCreatedBy().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_LESSON_ACCESS);
        }
    }

    private void validateMinigameRequest(MinigameChallengeRequest request) {
        if (request.getOrderIndex() == null || request.getOrderIndex() < 0 || request.getMaxScore() == null || request.getMaxScore() <= 0) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        if ("RHYTHM_MATCH".equals(request.getChallengeType())) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(request.getContentJson());
                JsonNode rounds = root.path("rounds");
                if (rounds.isArray() && rounds.size() > 0) {
                    for (JsonNode roundNode : rounds) {
                        int roundTempo = roundNode.path("tempo_bpm").asInt(roundNode.path("tempoBpm").asInt(root.path("tempo_bpm").asInt(root.path("tempoBpm").asInt(0))));
                        if (roundTempo <= 0) {
                            throw new AppException(ErrorCode.BAD_REQUEST);
                        }
                        JsonNode roundBeats = roundNode.path("beats");
                        if (!roundBeats.isArray() || roundBeats.size() == 0) {
                            throw new AppException(ErrorCode.BAD_REQUEST);
                        }
                        double prevBeat = -1.0;
                        for (JsonNode beatNode : roundBeats) {
                            if (!beatNode.isNumber()) {
                                throw new AppException(ErrorCode.BAD_REQUEST);
                            }
                            double beat = beatNode.asDouble();
                            if (beat < 0 || Double.isNaN(beat) || Double.isInfinite(beat) || beat <= prevBeat) {
                                throw new AppException(ErrorCode.BAD_REQUEST);
                            }
                            prevBeat = beat;
                        }
                    }
                } else {
                    int tempo = root.path("tempo_bpm").asInt(root.path("tempoBpm").asInt(0));
                    if (tempo <= 0) {
                        throw new AppException(ErrorCode.BAD_REQUEST);
                    }
                    JsonNode beats = root.path("beats");
                    if (!beats.isArray() || beats.size() == 0) {
                        throw new AppException(ErrorCode.BAD_REQUEST);
                    }
                    double prevBeat = -1.0;
                    for (JsonNode beatNode : beats) {
                        if (!beatNode.isNumber()) {
                            throw new AppException(ErrorCode.BAD_REQUEST);
                        }
                        double beat = beatNode.asDouble();
                        if (beat < 0 || Double.isNaN(beat) || Double.isInfinite(beat) || beat <= prevBeat) {
                            throw new AppException(ErrorCode.BAD_REQUEST);
                        }
                        prevBeat = beat;
                    }
                }
            } catch (AppException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            return;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(request.getContentJson());
            JsonNode melody = root.path("melody");
            JsonNode missingPositions = root.path("missing_positions");
            if (!melody.isArray() || melody.size() < 2 || !missingPositions.isArray() || missingPositions.size() != 1) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            int position = missingPositions.get(0).asInt(-1);
            if (position < 0 || position >= melody.size()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            JsonNode options = root.path("note_options").path(String.valueOf(position));
            String correct = root.path("correct_answers").path(String.valueOf(position)).asText("").trim();
            if (!options.isArray() || options.size() < 4 || correct.isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            Set<String> values = new HashSet<>();
            for (JsonNode option : options) {
                String value = option.asText("").trim();
                if (value.isEmpty() || !values.add(value)) {
                    throw new AppException(ErrorCode.BAD_REQUEST);
                }
            }
            if (!values.contains(correct) || root.path("bpm").asInt(0) <= 0 || root.path("time_limit_sec").asInt(0) <= 0) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }

    private String prepareContentJson(Lesson lesson, MinigameChallengeRequest request) {
        try {
            ObjectNode content = (ObjectNode) OBJECT_MAPPER.readTree(request.getContentJson());
            if (request.getReferenceAssetId() != null) {
                MediaAsset asset = mediaAssetRepository.findById(request.getReferenceAssetId())
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                if (!lesson.getId().equals(asset.getLesson().getId()) || !"REFERENCE_AUDIO".equals(asset.getAssetType())) {
                    throw new AppException(ErrorCode.BAD_REQUEST);
                }
                content.put("audio_asset_id", asset.getId());
                content.put("referenceAudioUrl", asset.getAssetUrl());
            } else {
                content.remove("audio_asset_id");
                content.remove("referenceAudioUrl");
            }
            return OBJECT_MAPPER.writeValueAsString(content);
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }
}
