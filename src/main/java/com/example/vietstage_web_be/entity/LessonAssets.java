package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lesson_assets")
public class LessonAssets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lessons lesson;

    @Column(name = "asset_type", nullable = false)
    private String assetType; // REFERENCE_AUDIO | SHEET_IMAGE | TECHNIQUE_VIDEO | BEAT_MAP

    @Column(name = "asset_url", nullable = false)
    private String assetUrl;

    @Column(name = "tempo_bpm")
    private Integer tempoBpm;

    @Column(name = "duration_sec")
    private BigDecimal durationSec;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
