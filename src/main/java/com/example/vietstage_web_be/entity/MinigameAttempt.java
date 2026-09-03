package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "minigame_attempts")
public class MinigameAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "minigame_challenge_id", nullable = false)
    private MinigameChallenge challenge;

    @Column(name = "score")
    private Integer score;

    @Column(name = "stars_earned")
    private Integer starsEarned;

    /** Reward snapshot, retained even when the XP-per-star setting changes later. */
    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "sync_status")
    private String syncStatus;

    @Column(name = "client_attempt_id", unique = true)
    private String clientAttemptId;
}
