package com.example.vietstage_web_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "exercises")
public class Exercises {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "exercise_type")
    private String exerciseType;

    @Column(name = "reference_notes")
    private String referenceNotes;

    @Column(name = "bpm")
    private Long bpm;

    @Column(name = "max_score")
    private Long maxScore;

    @Column(name = "created_at")
    private Date createdAt;

}
