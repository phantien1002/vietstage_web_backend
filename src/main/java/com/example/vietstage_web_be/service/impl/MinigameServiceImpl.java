package com.example.vietstage_web_be.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.vietstage_web_be.dto.request.MinigameAttemptRequest;
import com.example.vietstage_web_be.dto.request.MinigameChallengeRequest;
import com.example.vietstage_web_be.dto.response.MinigameAttemptResponse;
import com.example.vietstage_web_be.dto.response.MinigameChallengeResponse;
import com.example.vietstage_web_be.entity.Instrument;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.MinigameAttempt;
import com.example.vietstage_web_be.entity.MinigameChallenge;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.entity.MediaAsset;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.AppConfigRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.LearnerProfileRepository;
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
    private final AppConfigRepository appConfigRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<MinigameChallengeResponse> getMinigamesByLesson(Long lessonId, User requester) {
        if (isLearner(requester) && !isMinigameEnabled()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mini Game đang tạm thời bị tắt");
        }
        List<MinigameChallenge> challenges = challengeRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        return challenges.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MinigameChallengeResponse createMinigame(User actor, Long lessonId, MinigameChallengeRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        validateLessonOwnership(actor, lesson);
        validateMinigameRequest(lesson, request);

        if (challengeRepository.existsByLessonIdAndOrderIndex(lessonId, request.getOrderIndex())) {
            throw new AppException(ErrorCode.CONFLICT, "Thứ tự (order index " + request.getOrderIndex() + ") đã tồn tại trong bài học này");
        }

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
        MinigameChallenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MINIGAME_NOT_FOUND));
        validateLessonOwnership(actor, challenge.getLesson());
        validateMinigameRequest(challenge.getLesson(), request);

        if (!challenge.getOrderIndex().equals(request.getOrderIndex())
                && challengeRepository.existsByLessonIdAndOrderIndex(challenge.getLesson().getId(), request.getOrderIndex())) {
            throw new AppException(ErrorCode.CONFLICT, "Thứ tự (order index " + request.getOrderIndex() + ") đã tồn tại trong bài học này");
        }

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
        if (!isMinigameEnabled()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mini Game đang tạm thời bị tắt");
        }
        MinigameChallenge challenge = challengeRepository.findById(minigameId)
                .orElseThrow(() -> new AppException(ErrorCode.MINIGAME_NOT_FOUND));

        validateSubmittedAttempt(challenge, request);
        com.example.vietstage_web_be.entity.LearnerProfile profile = learnerProfileRepository.findByUserId(learner.getId()).orElse(null);

        int pointsPerStar = configInt("scoring.minigame.points_per_star", legacyPointsPerStar());

        if (request.getClientAttemptId() != null) {
            java.util.Optional<MinigameAttempt> existingAttempt = attemptRepository
                    .findByClientAttemptIdAndLearnerId(request.getClientAttemptId(), learner.getId());
            if (existingAttempt.isPresent()) {
                MinigameAttempt attempt = existingAttempt.get();
                if (!attempt.getChallenge().getId().equals(challenge.getId())) {
                    throw new AppException(ErrorCode.BAD_REQUEST, "Mã attempt đã được dùng cho Mini Game khác");
                }
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
                        .pointsEarned(attempt.getPointsEarned() != null ? attempt.getPointsEarned() : attempt.getStarsEarned() * 5)
                        .build();
            }
        }

        int starsEarned = calculateStars(request.getScore(), challenge.getMaxScore());

        Integer pointsEarned = starsEarned * pointsPerStar;

        MinigameAttempt attempt = MinigameAttempt.builder()
                .challenge(challenge)
                .learner(learner)
                .score(request.getScore())
                .starsEarned(starsEarned)
                .pointsEarned(pointsEarned)
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .clientAttemptId(request.getClientAttemptId())
                .build();

        attempt = attemptRepository.save(attempt);
        
        if (pointsEarned > 0) {
            leaderboardService.addPoints(learner, pointsEarned, "MINI_GAME");
        }
        
        // addPoints also provisions the learner profile for older accounts.
        // Reload it before applying the separately configured game stars.
        if (profile == null) {
            profile = learnerProfileRepository.findByUserId(learner.getId()).orElse(null);
        }
        if (profile != null && starsEarned > 0) {
            profile.setTotalStars(profile.getTotalStars() + starsEarned);
            profile.setSpendableStars(profile.getSpendableStars() + starsEarned);
            learnerProfileRepository.save(profile);
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
                .pointsEarned(attempt.getPointsEarned() != null ? attempt.getPointsEarned() : attempt.getStarsEarned() * 5)
                .build());
    }

    private boolean isMinigameEnabled() {
        return appConfigRepository.findByConfigKey("feature.minigame.enabled")
                .map(config -> Boolean.parseBoolean(config.getConfigValue()))
                .orElse(true);
    }

    private boolean isLearner(User user) {
        if (user == null) return false;
        try {
            return user.getRole() != null && "LEARNER".equalsIgnoreCase(user.getRole().getName());
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isUserAdmin(User user) {
        if (user == null) return false;
        try {
            return user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
        } catch (Exception ignored) {
            return false;
        }
    }

    private int configInt(String key, int fallback) {
        try {
            return appConfigRepository.findByConfigKey(key)
                    .map(config -> (int) Math.round(Double.parseDouble(config.getConfigValue())))
                    .orElse(fallback);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private int legacyPointsPerStar() {
        return configInt("scoring.minigame.multiplier", 5);
    }

    private int calculateStars(int score, int maxScore) {
        double percent = maxScore <= 0 ? 0 : score * 100.0 / maxScore;
        double star1 = configInt("scoring.minigame.star1_threshold", 50);
        double star2 = configInt("scoring.minigame.star2_threshold", 70);
        double star3 = configInt("scoring.minigame.star3_threshold", 90);
        if (percent >= star3) return 3;
        if (percent >= star2) return 2;
        return percent >= star1 ? 1 : 0;
    }

    private void validateSubmittedAttempt(MinigameChallenge challenge, MinigameAttemptRequest request) {
        if (request.getScore() < 0 || request.getScore() > challenge.getMaxScore()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Điểm Mini Game phải nằm trong khoảng 0 đến điểm tối đa");
        }
        if (request.getCompletedAt().isBefore(request.getStartedAt())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Thời điểm hoàn thành không được trước thời điểm bắt đầu");
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(challenge.getContentJson());
            int timeLimitSeconds = root.path("time_limit_sec").asInt(0);
            if (timeLimitSeconds > 0 && java.time.Duration.between(request.getStartedAt(), request.getCompletedAt()).getSeconds() > timeLimitSeconds) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Thời gian hoàn thành đã vượt giới hạn của Mini Game");
            }
        } catch (AppException exception) {
            throw exception;
        } catch (Exception ignored) {
            // Existing rhythm challenges do not require a time limit.
        }
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
        if (actor != null && isUserAdmin(actor)) {
            return;
        }
        if (actor == null || lesson.getCreatedBy() == null || !actor.getId().equals(lesson.getCreatedBy().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_LESSON_ACCESS);
        }
    }

    private void validateMinigameRequest(Lesson lesson, MinigameChallengeRequest request) {
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
                        JsonNode events = roundNode.path("events");
                        if (events.isArray() && events.size() > 0) {
                            validateRhythmEvents(lesson, events);
                            continue;
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
                        JsonNode roundNotes = roundNode.path("notes");
                        if (!roundNotes.isArray() || roundNotes.size() != roundBeats.size() || roundNotes.size() < 2) {
                            throw new AppException(ErrorCode.BAD_REQUEST);
                        }
                        for (JsonNode noteNode : roundNotes) {
                            if (!isSupportedRhythmNote(lesson, noteNode.asText(""))) {
                                throw new AppException(ErrorCode.BAD_REQUEST);
                            }
                        }
                        JsonNode roundDurations = roundNode.path("durations");
                        if (roundDurations.isArray() && roundDurations.size() > 0 && roundDurations.size() != roundBeats.size()) {
                            throw new AppException(ErrorCode.BAD_REQUEST);
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
                    JsonNode notes = root.path("notes");
                    if (!notes.isArray() || notes.size() != beats.size() || notes.size() < 2) {
                        throw new AppException(ErrorCode.BAD_REQUEST);
                    }
                    for (JsonNode noteNode : notes) {
                        if (!isSupportedRhythmNote(lesson, noteNode.asText(""))) {
                            throw new AppException(ErrorCode.BAD_REQUEST);
                        }
                    }
                    JsonNode durations = root.path("durations");
                    if (durations.isArray() && durations.size() > 0 && durations.size() != beats.size()) {
                        throw new AppException(ErrorCode.BAD_REQUEST);
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

    /** Validates the authored event timeline used by the redesigned rhythm editor. */
    private void validateRhythmEvents(Lesson lesson, JsonNode events) {
        if (events.size() < 2 || events.size() > 16) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        boolean hasTarget = false;
        long previousAtMs = -1L;
        for (JsonNode event : events) {
            String mode = event.path("mode").asText("");
            if (!"SAMPLE".equals(mode) && !"TARGET".equals(mode)) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            hasTarget |= "TARGET".equals(mode);
            if (!isSupportedRhythmNote(lesson, event.path("note").asText(""))) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            double durationBeats = event.path("duration_beats").asDouble(0.0);
            if (durationBeats != 0.25 && durationBeats != 0.5 && durationBeats != 1.0
                    && durationBeats != 2.0 && durationBeats != 4.0) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            if (event.has("at_ms")) {
                long atMs = event.path("at_ms").asLong(-1L);
                if (atMs < 0 || atMs <= previousAtMs) {
                    throw new AppException(ErrorCode.BAD_REQUEST);
                }
                previousAtMs = atMs;
            }
        }
        if (!hasTarget) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }

    private boolean isSupportedRhythmNote(Lesson lesson, String rawNote) {
        if (lesson == null || rawNote == null || rawNote.trim().isEmpty()) {
            return false;
        }
        String note = rawNote.trim();
        String instrumentKey = resolveInstrumentKey(lesson.getInstrument());
        Set<String> supported = switch (instrumentKey) {
            // Đàn tranh 17 dây, dây Bắc: Sol–La–Đô–Rê–Mi theo thứ tự dây 1 → 17 (và dạng ký hiệu quốc tế tương đương).
            case "dan_tranh" -> Set.of(
                    "Sol1", "La1", "Đô2", "Rê2", "Mi2", "Sol2", "La2", "Đô3", "Rê3", "Mi3", "Sol3", "La3", "Đô4", "Rê4", "Mi4", "Sol4", "La4",
                    "G1", "A1", "C2", "D2", "E2", "G2", "A2", "C3", "D3", "E3", "G3", "A3", "C4", "D4", "E4", "G4", "A4"
            );
            case "dan_bau" -> Set.of("C4", "D4", "E4", "G4", "A4", "C5", "E5", "G5", "Đô4", "Rê4", "Mi4", "Sol4", "La4", "Đô5", "Mi5", "Sol5");
            case "sao_truc" -> Set.of("Đô", "Rê", "Mi", "Fa", "Sol", "La", "Si", "Đố", "C4", "D4", "E4", "F4", "G4", "A4", "B4", "C5");
            case "trong_chau" -> Set.of("Tịch", "Cắc", "Tung", "Rong");
            default -> Set.of();
        };
        return supported.isEmpty() || supported.contains(note);
    }

    private String resolveInstrumentKey(Instrument instrument) {
        if (instrument == null) return "dan_tranh";
        String combined = ((instrument.getInstrumentCode() != null ? instrument.getInstrumentCode() : "") + " "
                + (instrument.getName() != null ? instrument.getName() : "")).toLowerCase();
        if (combined.contains("tranh") || combined.contains("dan_tranh")) return "dan_tranh";
        if (combined.contains("bau") || combined.contains("bầu") || combined.contains("dan_bau")) return "dan_bau";
        if (combined.contains("sao") || combined.contains("sáo") || combined.contains("sao_truc")) return "sao_truc";
        if (combined.contains("trong") || combined.contains("trống") || combined.contains("trong_chau")) return "trong_chau";
        return "dan_tranh";
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
