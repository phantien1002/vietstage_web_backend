package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id")
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "selected_answer")
    private String selectedAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "score")
    private BigDecimal score;

    /** Reward snapshot. Historical attempts must not be recalculated after an admin changes a rule. */
    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "stars_earned")
    private Integer starsEarned;

    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt;

    @Column(name = "client_attempt_id", unique = true)
    private String clientAttemptId;
}
