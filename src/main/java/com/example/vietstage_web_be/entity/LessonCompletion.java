package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "learner_lesson_progress")
public class LessonCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // Surrogate key added for JpaRepository

    @ManyToOne
    @JoinColumn(name = "learner_user_id", nullable = false)
    private User learner;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "status", nullable = false)
    private String status = "LOCKED";

    // Map `completed` for queries
    @Transient
    private Boolean completed;
    
    public Boolean getCompleted() {
        return "COMPLETED".equals(this.status);
    }
    
    public void setCompleted(Boolean completed) {
        this.completed = completed;
        if (Boolean.TRUE.equals(completed)) {
            this.status = "COMPLETED";
        }
    }

    @Column(name = "stars", nullable = false)
    private Integer stars = 0;

    @Column(name = "best_score")
    private BigDecimal bestScore;

    @Column(name = "unlocked_at")
    private Date unlockedAt;

    @Column(name = "started_at")
    private Date startedAt;

    @Column(name = "completed_at")
    private Date completedAt;

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;
}
