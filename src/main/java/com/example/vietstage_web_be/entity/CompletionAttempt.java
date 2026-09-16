package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "completion_attempt")
@IdClass(CompletionAttempt.CompletionAttemptId.class)
public class CompletionAttempt {

    @Id
    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Id
    @Column(name = "client_attempt_id", nullable = false)
    private String clientAttemptId;

    @Column(name = "lesson_code", nullable = false)
    private String lessonCode;

    @Column(name = "revision", nullable = false)
    private Integer revision;

    @Column(name = "score", nullable = false)
    private BigDecimal score;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime receivedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionAttemptId implements Serializable {
        private Long learnerId;
        private String clientAttemptId;
    }
}
