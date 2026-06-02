package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "learner_progress")
public class LearnerProgress {
    @EmbeddedId
    private LearnerProgressId id = new LearnerProgressId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("learnerId")
    @JoinColumn(name = "learner_id", foreignKey = @ForeignKey(name = "fk_progress_learner"))
    private Users learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lessonId")
    @JoinColumn(name = "lesson_id", foreignKey = @ForeignKey(name = "fk_progress_lesson"))
    private Lessons lesson;

    @Column(name = "stars")
    private Integer stars;

    @Column(name = "completed")
    private Boolean completed;

}
