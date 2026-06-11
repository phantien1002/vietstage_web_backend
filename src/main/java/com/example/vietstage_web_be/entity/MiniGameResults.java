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
@Table(name = "mini_game_results")
public class MiniGameResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "learner_id", nullable = false)
    private Users learner;

    @ManyToOne
    @JoinColumn(name = "mini_game_id", nullable = false)
    private MiniGames miniGame;

    @Column(name = "score")
    private Integer score;

    @Column(name = "stars_earned")
    private Integer starsEarned;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "sync_status")
    private String syncStatus; // SYNCED | PENDING_SYNC | CONFLICT

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
