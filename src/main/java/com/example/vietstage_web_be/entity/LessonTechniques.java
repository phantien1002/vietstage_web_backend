package com.example.vietstage_web_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lesson_techniques")
public class LessonTechniques {

    @Id
    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Id
    @Column(name = "technique_id", nullable = false)
    private Long techniqueId;

}
