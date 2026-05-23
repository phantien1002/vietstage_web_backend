package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "learner_progress")
public class LearnerProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private Users learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lessons lesson;

    @Column(name = "completion_percentage")
    private BigDecimal completionPercentage;

    @Column(name = "highest_score")
    private BigDecimal highestScore;

    @Column(name = "total_practice_time")
    private Long totalPracticeTime;

    @Column(name = "is_completed")
    private Boolean completed;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
