package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lesson_revision")
@IdClass(LessonRevision.LessonRevisionId.class)
public class LessonRevision {

    @Id
    @Column(name = "lesson_code", nullable = false)
    private String lessonCode;

    @Id
    @Column(name = "revision", nullable = false)
    private Integer revision;

    @Column(name = "content_document", columnDefinition = "jsonb", nullable = false)
    private String contentDocument;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonRevisionId implements Serializable {
        private String lessonCode;
        private Integer revision;
    }
}
