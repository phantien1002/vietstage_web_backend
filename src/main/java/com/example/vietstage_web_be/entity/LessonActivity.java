package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lesson_activity", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"lesson_code", "revision", "order_index"})
})
public class LessonActivity {

    @Id
    @Column(name = "activity_code", nullable = false)
    private String activityCode;

    @Column(name = "lesson_code", nullable = false)
    private String lessonCode;

    @Column(name = "revision", nullable = false)
    private Integer revision;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "activity_type", nullable = false)
    private String activityType;

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "config", columnDefinition = "jsonb", nullable = false)
    private String config = "{}";
}
