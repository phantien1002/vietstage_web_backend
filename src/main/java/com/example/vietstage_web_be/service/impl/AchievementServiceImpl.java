package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.AchievementRequest;
import com.example.vietstage_web_be.dto.response.AchievementResponse;
import com.example.vietstage_web_be.dto.response.LearnerAchievementsResponse;
import com.example.vietstage_web_be.entity.Achievement;
import com.example.vietstage_web_be.entity.LearnerAchievement;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.AchievementRepository;
import com.example.vietstage_web_be.repository.LearnerAchievementRepository;
import com.example.vietstage_web_be.service.IAchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements IAchievementService {

    private final AchievementRepository achievementRepository;
    private final LearnerAchievementRepository learnerAchievementRepository;

    @Override
    public List<AchievementResponse> getAllAchievements() {
        return achievementRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AchievementResponse createAchievement(AchievementRequest request) {
        Achievement achievement = Achievement.builder()
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .conditionJson(request.getConditionJson())
                .build();
        achievement = achievementRepository.save(achievement);
        return mapToResponse(achievement);
    }

    @Override
    public AchievementResponse updateAchievement(Long id, AchievementRequest request) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACHIEVEMENT_NOT_FOUND));

        achievement.setName(request.getName());
        achievement.setDescription(request.getDescription());
        achievement.setIconUrl(request.getIconUrl());
        achievement.setConditionJson(request.getConditionJson());

        achievement = achievementRepository.save(achievement);
        return mapToResponse(achievement);
    }

    @Override
    public LearnerAchievementsResponse getMyAchievements(User learner) {
        List<Achievement> allAchievements = achievementRepository.findAll();
        List<LearnerAchievement> learnerAchievements = learnerAchievementRepository.findByLearnerId(learner.getId());

        Set<Long> earnedAchievementIds = learnerAchievements.stream()
                .map(la -> la.getAchievement().getId())
                .collect(Collectors.toSet());

        List<AchievementResponse> earned = learnerAchievements.stream()
                .map(la -> AchievementResponse.builder()
                        .id(la.getAchievement().getId())
                        .name(la.getAchievement().getName())
                        .description(la.getAchievement().getDescription())
                        .iconUrl(la.getAchievement().getIconUrl())
                        .conditionJson(la.getAchievement().getConditionJson())
                        .earnedAt(la.getEarnedAt())
                        .build())
                .collect(Collectors.toList());

        List<AchievementResponse> locked = allAchievements.stream()
                .filter(a -> !earnedAchievementIds.contains(a.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return LearnerAchievementsResponse.builder()
                .earned(earned)
                .locked(locked)
                .build();
    }

    @Override
    public void revokeAchievement(Long learnerId, Long achievementId) {
        LearnerAchievement la = learnerAchievementRepository.findByLearnerIdAndAchievementId(learnerId, achievementId)
                .orElseThrow(() -> new AppException(ErrorCode.ACHIEVEMENT_NOT_FOUND));
        learnerAchievementRepository.delete(la);
    }

    private AchievementResponse mapToResponse(Achievement achievement) {
        return AchievementResponse.builder()
                .id(achievement.getId())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .iconUrl(achievement.getIconUrl())
                .conditionJson(achievement.getConditionJson())
                .build();
    }
}
