package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "point_transactions")
public class PointTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "learner_id", nullable = false)
    private Users learner;

    @Column(name = "source_type", nullable = false)
    private String sourceType; // LESSON | MINI_GAME | DAILY_CHALLENGE | ACHIEVEMENT

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
