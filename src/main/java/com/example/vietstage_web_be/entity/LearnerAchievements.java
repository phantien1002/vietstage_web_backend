package com.example.vietstage_web_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "learner_achievements")
public class LearnerAchievements {

    @Id
    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Id
    @Column(name = "achievement_id", nullable = false)
    private Long achievementId;

}
