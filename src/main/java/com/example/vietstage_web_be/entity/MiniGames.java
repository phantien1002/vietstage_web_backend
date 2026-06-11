package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
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

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "game_type", length = 50)
    private String gameType; // NOTE_QUIZ | RHYTHM_MATCH | MELODY_COMPLETE

    @Column(name = "difficulty", length = 50)
    private String difficulty; // EASY | MEDIUM | HARD

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Liên kết ngược với MiniGameResults (từ main)
    @OneToMany(mappedBy = "miniGame")
    private List<MiniGameResults> results;

    // Liên kết nhiều-nhiều với Lessons (từ Tai - lesson_mini_games)
    @ManyToMany(mappedBy = "miniGames")
    private Set<Lessons> lessons;
}
