package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.DailyChallengeRequest;
import com.example.vietstage_web_be.dto.response.CompletionResponse;
import com.example.vietstage_web_be.dto.response.DailyChallengeLearnerResponse;
import com.example.vietstage_web_be.dto.response.DailyChallengeResponse;
import com.example.vietstage_web_be.entity.*;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.*;
import com.example.vietstage_web_be.service.IDailyChallengeService;
import com.example.vietstage_web_be.service.ILearnerProgressService;
import com.example.vietstage_web_be.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyChallengeServiceImpl implements IDailyChallengeService {

    private final DailyChallengeRepository dailyChallengeRepository;
    private final LearnerDailyChallengeRepository learnerDailyChallengeRepository;
    private final InstrumentRepository instrumentRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ILearnerProgressService progressService;
    private final INotificationService notificationService;

    @Override
    public List<DailyChallengeLearnerResponse> getChallenges(LocalDate date, User learner) {
        if (date == null) {
            date = LocalDate.now();
        }
        List<DailyChallenge> challenges = dailyChallengeRepository.findByChallengeDate(date);
        return challenges.stream().map(challenge -> {
            Optional<LearnerDailyChallenge> completionOpt = 
                learnerDailyChallengeRepository.findByLearnerIdAndChallengeId(learner.getId(), challenge.getId());
            
            return DailyChallengeLearnerResponse.builder()
                    .id(challenge.getId())
                    .title(challenge.getTitle())
                    .description(challenge.getDescription())
                    .instrumentId(challenge.getInstrument().getId())
                    .rewardPoints(challenge.getRewardPoints())
                    .challengeDate(challenge.getChallengeDate())
                    .isCompleted(completionOpt.isPresent())
                    .completedAt(completionOpt.map(LearnerDailyChallenge::getCompletedAt).orElse(null))
                    .pointsEarned(completionOpt.map(LearnerDailyChallenge::getPointsEarned).orElse(0))
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public DailyChallengeResponse createChallenge(DailyChallengeRequest request) {
        Instrument instrument = instrumentRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));

        DailyChallenge challenge = DailyChallenge.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instrument(instrument)
                .rewardPoints(request.getRewardPoints())
                .challengeDate(request.getChallengeDate())
                .build();
        
        dailyChallengeRepository.save(challenge);
        return mapToResponse(challenge);
    }

    @Override
    @Transactional
    public DailyChallengeResponse updateChallenge(Long id, DailyChallengeRequest request) {
        DailyChallenge challenge = dailyChallengeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Instrument instrument = instrumentRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));

        challenge.setTitle(request.getTitle());
        challenge.setDescription(request.getDescription());
        challenge.setInstrument(instrument);
        challenge.setRewardPoints(request.getRewardPoints());
        challenge.setChallengeDate(request.getChallengeDate());

        dailyChallengeRepository.save(challenge);
        return mapToResponse(challenge);
    }

    @Override
    @Transactional
    public void deleteChallenge(Long id) {
        DailyChallenge challenge = dailyChallengeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        dailyChallengeRepository.delete(challenge);
    }

    @Override
    @Transactional
    public CompletionResponse completeChallenge(Long id, User learner) {
        DailyChallenge challenge = dailyChallengeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (learnerDailyChallengeRepository.existsByLearnerIdAndChallengeId(learner.getId(), challenge.getId())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        LearnerDailyChallenge completion = LearnerDailyChallenge.builder()
                .learner(learner)
                .challenge(challenge)
                .completedAt(LocalDateTime.now())
                .pointsEarned(challenge.getRewardPoints())
                .build();
        learnerDailyChallengeRepository.save(completion);

        LearnerProfile profile = learnerProfileRepository.findByUserId(learner.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Update profile points
        Integer currentPoints = profile.getTotalPoints() != null ? profile.getTotalPoints() : 0;
        profile.setTotalPoints(currentPoints + challenge.getRewardPoints());
        
        // Update streak logic using shared service
        progressService.updateStreakAndSave(profile);

        // Notify user
        notificationService.createNotification(learner,
                "Thử thách hằng ngày hoàn tất!",
                "Chúc mừng! Bạn đã hoàn thành thử thách '" + challenge.getTitle() + "' và nhận được " + challenge.getRewardPoints() + " điểm.",
                "DAILY_CHALLENGE"
        );

        // Record transaction
        PointTransaction transaction = PointTransaction.builder()
                .user(learner)
                .points(challenge.getRewardPoints())
                .sourceType("DAILY_CHALLENGE")
                .createdAt(LocalDateTime.now())
                .build();
        pointTransactionRepository.save(transaction);

        // TODO: Update Leaderboard in Redis

        return CompletionResponse.builder()
                .challengeId(challenge.getId())
                .completedAt(completion.getCompletedAt())
                .pointsEarned(completion.getPointsEarned())
                .build();
    }

    private DailyChallengeResponse mapToResponse(DailyChallenge challenge) {
        return DailyChallengeResponse.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .instrumentId(challenge.getInstrument().getId())
                .rewardPoints(challenge.getRewardPoints())
                .challengeDate(challenge.getChallengeDate())
                .build();
    }
}
