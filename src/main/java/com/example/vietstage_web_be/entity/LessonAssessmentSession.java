package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "lesson_assessment_sessions", uniqueConstraints = @UniqueConstraint(
        name = "uk_assessment_session_learner_client", columnNames = {"learner_id", "client_session_id"}))
public class LessonAssessmentSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "learner_id", nullable = false)
    private User learner;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    @Column(name = "client_session_id", nullable = false)
    private String clientSessionId;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal score;
    @Column(name = "max_score", nullable = false, precision = 8, scale = 2)
    private BigDecimal maxScore;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal accuracy;
    @Column(name = "stars_earned", nullable = false)
    private Integer starsEarned;
    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<LessonAssessmentQuizAnswer> quizAnswers = new ArrayList<>();
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<LessonAssessmentMinigameResult> minigameResults = new ArrayList<>();
}
