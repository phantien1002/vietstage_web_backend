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
@Table(name = "lessons")
public class Lessons {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "skill_level_id", nullable = false)
    private Long skillLevelId;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "duration_minutes")
    private Long durationMinutes;

    @Column(name = "unlock_score")
    private Long unlockScore;

    @Column(name = "is_published")
    private Boolean published;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

}
