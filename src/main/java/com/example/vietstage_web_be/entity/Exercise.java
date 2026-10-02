package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exercises")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "pass_threshold")
    private BigDecimal passThreshold;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "beat_map_asset_id")
    private MediaAsset beatMapAsset;

    @ManyToOne
    @JoinColumn(name = "reference_asset_id")
    private MediaAsset referenceAsset;

    @Column(name = "exercise_type")
    private String exerciseType;

    @Column(name = "practice_mode")
    private String practiceMode;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "schema_version")
    private String schemaVersion;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL)
    private List<PracticeAttempt> practiceAttempts;
}
