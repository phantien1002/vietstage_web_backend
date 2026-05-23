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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private Users learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mini_game_id", nullable = false)
    private MiniGames miniGame;

    @Column(name = "score")
    private Long score;

    @Column(name = "stars_earned")
    private Long starsEarned;

    @Column(name = "played_at")
    private LocalDateTime playedAt;

}
