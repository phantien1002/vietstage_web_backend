package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mini_games")
public class MiniGames {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "game_type")
    private String gameType; // NOTE_QUIZ | RHYTHM_MATCH | MELODY_COMPLETE

    @Column(name = "difficulty")
    private String difficulty; // EASY | MEDIUM | HARD

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "miniGames")
    private Set<Lessons> lessons;
}
