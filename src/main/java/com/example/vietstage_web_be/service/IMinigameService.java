package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.MinigameAttemptRequest;
import com.example.vietstage_web_be.dto.request.MinigameChallengeRequest;
import com.example.vietstage_web_be.dto.response.MinigameAttemptResponse;
import com.example.vietstage_web_be.dto.response.MinigameChallengeResponse;
import com.example.vietstage_web_be.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IMinigameService {
    List<MinigameChallengeResponse> getMinigamesByLesson(Long lessonId, User currentUser);
    MinigameChallengeResponse createMinigame(Long lessonId, MinigameChallengeRequest request);
    MinigameChallengeResponse updateMinigame(Long id, MinigameChallengeRequest request);
    void deleteMinigame(Long id);
    
    MinigameAttemptResponse submitAttempt(Long minigameId, MinigameAttemptRequest request, User learner);
    Page<MinigameAttemptResponse> getAttempts(Long minigameId, Pageable pageable, User learner);
}