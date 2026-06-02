package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private Users user;
}
