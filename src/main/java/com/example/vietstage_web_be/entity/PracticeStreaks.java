package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "practice_streaks")
public class PracticeStreaks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", referencedColumnName = "id", nullable = false, unique = true)
    private Users learner;

    @Column(name = "current_streak")
    private Long currentStreak;

    @Column(name = "longest_streak")
    private Long longestStreak;

    @Column(name = "total_practice_days")
    private Long totalPracticeDays;

    @Column(name = "last_practice_date")
    private LocalDateTime lastPracticeDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
