package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.LeaderboardEntryResponse;
import com.example.vietstage_web_be.dto.response.MyLeaderboardResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.PointTransactionResponse;
import com.example.vietstage_web_be.entity.LearnerProfile;
import com.example.vietstage_web_be.entity.PointTransaction;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.repository.LearnerProfileRepository;
import com.example.vietstage_web_be.repository.PointTransactionRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.service.ILeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements ILeaderboardService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PointTransactionRepository pointTransactionRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final UserRepository userRepository;

    private static final String LEADERBOARD_KEY = "leaderboard:total_points";

    @Override
    public List<LeaderboardEntryResponse> getTopLeaderboard(int top) {
        initLeaderboardIfEmpty();

        Set<ZSetOperations.TypedTuple<Object>> topUsers = redisTemplate.opsForZSet()
                .reverseRangeWithScores(LEADERBOARD_KEY, 0, top - 1);

        List<LeaderboardEntryResponse> result = new ArrayList<>();
        if (topUsers == null) return result;

        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : topUsers) {
            String userIdStr = String.valueOf(tuple.getValue());
            Long userId = Long.valueOf(userIdStr);
            Double score = tuple.getScore();

            User user = userRepository.findById(userId).orElse(null);
            LearnerProfile profile = learnerProfileRepository.findById(userId).orElse(null);

            if (user != null && profile != null) {
                result.add(LeaderboardEntryResponse.builder()
                        .rank(rank++)
                        .learnerName(user.getFullName())
                        .totalPoints(score != null ? score.intValue() : 0)
                        .currentStreak(profile.getCurrentStreak())
                        .build());
            }
        }
        return result;
    }

    @Override
    public MyLeaderboardResponse getMyLeaderboard(User learner) {
        initLeaderboardIfEmpty();

        String userIdStr = String.valueOf(learner.getId());
        Long rankIndex = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, userIdStr);
        Double score = redisTemplate.opsForZSet().score(LEADERBOARD_KEY, userIdStr);
        Long totalUsers = redisTemplate.opsForZSet().zCard(LEADERBOARD_KEY);

        int rank = (rankIndex != null) ? rankIndex.intValue() + 1 : 0;
        int totalPoints = (score != null) ? score.intValue() : 0;
        double percentile = 0.0;

        if (totalUsers != null && totalUsers > 0 && rank > 0) {
            percentile = ((double) (totalUsers - rank) / totalUsers) * 100.0;
        }

        return MyLeaderboardResponse.builder()
                .rank(rank)
                .totalPoints(totalPoints)
                .percentile(percentile)
                .build();
    }

    @Override
    public PageResponse<PointTransactionResponse> getPointTransactions(Long userId, String sourceType, Pageable pageable) {
        Page<PointTransaction> page;
        if (sourceType != null && !sourceType.isBlank()) {
            page = pointTransactionRepository.findByUserIdAndSourceType(userId, sourceType, pageable);
        } else {
            page = pointTransactionRepository.findByUserId(userId, pageable);
        }

        List<PointTransactionResponse> responses = page.getContent().stream().map(pt ->
                PointTransactionResponse.builder()
                        .id(pt.getId())
                        .sourceType(pt.getSourceType())
                        .points(pt.getPoints())
                        .createdAt(pt.getCreatedAt())
                        .build()
        ).toList();

        return PageResponse.<PointTransactionResponse>builder()
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
    public void addPoints(User learner, int points, String sourceType) {
        // XP and stars are separate rewards. Quiz/Mini Game services apply
        // their configured stars explicitly after this XP transaction.
        if (points == 0) return;

        // 1. Create audit log
        PointTransaction pt = PointTransaction.builder()
                .user(learner)
                .sourceType(sourceType)
                .points(points)
                .build();
        pointTransactionRepository.save(pt);

        // 2. Update learner profile
        Optional<LearnerProfile> profileOpt = learnerProfileRepository.findById(learner.getId());
        LearnerProfile profile = profileOpt.orElseGet(() -> {
            LearnerProfile newProfile = LearnerProfile.builder()
                    .userId(learner.getId())
                    .user(learner)
                    .build();
            return newProfile;
        });
        profile.setTotalPoints(profile.getTotalPoints() + points);
        
        learnerProfileRepository.save(profile);

        // 3. Update Redis ZSET
        redisTemplate.opsForZSet().add(LEADERBOARD_KEY, String.valueOf(learner.getId()), profile.getTotalPoints());
    }

    private void initLeaderboardIfEmpty() {
        Long size = redisTemplate.opsForZSet().zCard(LEADERBOARD_KEY);
        if (size == null || size == 0) {
            List<LearnerProfile> allProfiles = learnerProfileRepository.findAll();
            for (LearnerProfile profile : allProfiles) {
                redisTemplate.opsForZSet().add(LEADERBOARD_KEY, String.valueOf(profile.getUserId()), profile.getTotalPoints());
            }
        }
    }
}
