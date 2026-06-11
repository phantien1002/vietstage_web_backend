package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "learner_profiles")
public class LearnerProfiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "skill_level_id")
    private SkillLevels skillLevel;

    @Column(name = "favorite_instrument")
    private String favoriteInstrument;

    @Column(name = "total_practice_minutes")
    private Long totalPracticeMinutes;

    // Streak system
    @Column(name = "current_streak")
    private Integer currentStreak;

    @Column(name = "longest_streak")
    private Integer longestStreak;

    @Column(name = "last_practice_date")
    private LocalDate lastPracticeDate;

    // Adaptive difficulty
    @Column(name = "adaptive_difficulty")
    private String adaptiveDifficulty;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private Users user;
}
