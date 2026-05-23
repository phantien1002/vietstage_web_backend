package com.example.vietstage_web_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "learner_progress")
public class LearnerProgress {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "completion_percentage")
    private BigDecimal completionPercentage;

    @Column(name = "highest_score")
    private BigDecimal highestScore;

    @Column(name = "total_practice_time")
    private Long totalPracticeTime;

    @Column(name = "is_completed")
    private Boolean completed;

    @Column(name = "updated_at")
    private Date updatedAt;

}
