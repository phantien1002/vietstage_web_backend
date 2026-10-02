package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exercise_config")
public class ExerciseConfig {

    @Id
    @Column(name = "exercise_code", nullable = false)
    private String exerciseCode;

    @Column(name = "activity_code", nullable = false, unique = true)
    private String activityCode;

    @Column(name = "practice_mode", nullable = false)
    private String practiceMode;

    @Column(name = "schema_version", nullable = false)
    private Integer schemaVersion;

    @Column(name = "pass_threshold")
    private BigDecimal passThreshold;

    @Column(name = "practice_config", columnDefinition = "jsonb", nullable = false)
    private String practiceConfig;

    @Column(name = "recognition_profile_version", nullable = false)
    private String recognitionProfileVersion;
}
