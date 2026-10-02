package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lesson_legacy_key")
@IdClass(LessonLegacyKey.LessonLegacyKeyId.class)
public class LessonLegacyKey {

    @Id
    @Column(name = "lesson_code", nullable = false)
    private String lessonCode;

    @Id
    @Column(name = "activity_kind", nullable = false)
    private String activityKind;

    @Column(name = "legacy_code", nullable = false)
    private String legacyCode;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonLegacyKeyId implements Serializable {
        private String lessonCode;
        private String activityKind;
    }
}
