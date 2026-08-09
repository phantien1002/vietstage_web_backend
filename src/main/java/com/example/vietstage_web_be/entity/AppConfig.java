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
@Table(name = "app_configs")
public class AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true)
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private String configValue;

    @Column(name = "config_group")
    private String configGroup;

    @Column(name = "description")
    private String description;

    @Column(name = "value_type")
    private String valueType; // NUMBER, BOOLEAN, STRING, JSON

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "step_value")
    private Double stepValue;

    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    @Column(name = "default_value")
    private String defaultValue;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "is_public")
    private Boolean isPublic;
}
