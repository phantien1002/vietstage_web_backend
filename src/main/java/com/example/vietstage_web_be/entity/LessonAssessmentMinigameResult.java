package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "lesson_assessment_minigame_results", uniqueConstraints = @UniqueConstraint(
        name = "uk_assessment_session_minigame", columnNames = {"session_id", "minigame_id"}))
public class LessonAssessmentMinigameResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id", nullable = false) private LessonAssessmentSession session;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "minigame_id", nullable = false) private MinigameChallenge minigame;
    @Column(nullable = false, precision = 8, scale = 2) private BigDecimal score;
    @Column(name = "max_score", nullable = false, precision = 8, scale = 2) private BigDecimal maxScore;
    @Column(name = "started_at") private LocalDateTime startedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;
}
