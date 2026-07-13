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
@Table(name = "lesson_completions")
public class LessonCompletions {

    @EmbeddedId
    @Builder.Default
    private LessonCompletionsId id = new LessonCompletionsId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("learnerId")
    @JoinColumn(name = "learner_id")
    private Users learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lessonId")
    @JoinColumn(name = "lesson_id")
    private Lessons lesson;

    @Column(name = "stars")
    private Integer stars; // 0 to 3

    @Column(name = "completed")
    private Boolean completed;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "sync_status")
    private String syncStatus;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
