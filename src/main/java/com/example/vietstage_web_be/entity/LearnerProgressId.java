package com.example.vietstage_web_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerProgressId {
    @Column(name = "learner_id")
    private Long learnerId;

    @Column(name = "lesson_id")
    private Long lessonId;
}
