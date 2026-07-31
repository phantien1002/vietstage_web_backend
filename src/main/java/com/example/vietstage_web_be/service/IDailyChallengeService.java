package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.DailyChallengeRequest;
import com.example.vietstage_web_be.dto.response.CompletionResponse;
import com.example.vietstage_web_be.dto.response.DailyChallengeLearnerResponse;
import com.example.vietstage_web_be.dto.response.DailyChallengeResponse;
import com.example.vietstage_web_be.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface IDailyChallengeService {
    List<DailyChallengeLearnerResponse> getChallenges(LocalDate date, User learner);
    DailyChallengeResponse createChallenge(DailyChallengeRequest request);
    DailyChallengeResponse updateChallenge(Long id, DailyChallengeRequest request);
    void deleteChallenge(Long id);
    CompletionResponse completeChallenge(Long id, User learner);
}