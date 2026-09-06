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
@Table(
        name = "quiz_attempts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_quiz_attempt_learner_client",
                columnNames = {"learner_id", "client_attempt_id"}
        )
)
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

    /** Idempotency key scoped to one learner, so another learner cannot collide. */
    @Column(name = "client_attempt_id")
    private String clientAttemptId;
}
