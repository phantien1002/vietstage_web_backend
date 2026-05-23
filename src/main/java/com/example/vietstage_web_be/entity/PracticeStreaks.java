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
@Table(name = "practice_streaks")
public class PracticeStreaks {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Column(name = "current_streak")
    private Long currentStreak;

    @Column(name = "longest_streak")
    private Long longestStreak;

    @Column(name = "total_practice_days")
    private Long totalPracticeDays;

    @Column(name = "last_practice_date")
    private Date lastPracticeDate;

    @Column(name = "updated_at")
    private Date updatedAt;

}
