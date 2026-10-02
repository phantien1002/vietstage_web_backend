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
@Table(name = "media_assets")
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "asset_type", nullable = false)
    private String assetType;

    @Column(name = "title")
    private String title;

    @Column(name = "asset_url", nullable = false)
    private String assetUrl;

    @Column(name = "tempo_bpm")
    private Integer tempoBpm;

    @Column(name = "duration_sec")
    private BigDecimal durationSec;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "version")
    private Integer version;

    @Column(name = "processing_status")
    private String processingStatus;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
