package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.LeaderboardEntryResponse;
import com.example.vietstage_web_be.dto.response.MyLeaderboardResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.PointTransactionResponse;
import com.example.vietstage_web_be.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ILeaderboardService {
    List<LeaderboardEntryResponse> getTopLeaderboard(int top);
    MyLeaderboardResponse getMyLeaderboard(User learner);
    PageResponse<PointTransactionResponse> getPointTransactions(Long userId, String sourceType, Pageable pageable);
    void addPoints(User learner, int points, String sourceType);
}