package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "practice_attempts")
public class PracticeAttempts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private PracticeSessions session;

    @ManyToOne
    @JoinColumn(name = "learner_id")
    private Users learner;

    @ManyToOne
    @JoinColumn(name = "exercise_id")
    private Exercises exercise;

    @Column(name = "pitch_score")
    private BigDecimal pitchScore;

    @Column(name = "rhythm_score")
    private BigDecimal rhythmScore;

    @Column(name = "dynamics_score")
    private BigDecimal dynamicsScore;

    @Column(name = "tonal_quality_score")
    private BigDecimal tonalQualityScore;

    @Column(name = "breath_score")
    private BigDecimal breathScore;

    @Column(name = "total_score")
    private BigDecimal totalScore;

    @Column(name = "sync_status")
    private String syncStatus; // SYNCED | PENDING_SYNC | CONFLICT

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "practiceAttempt", cascade = CascadeType.ALL)
    private List<InstructorFeedback> feedbacks;
}
