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
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "practice_attempts")
public class PracticeAttempts {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(name = "audio_reference_id")
    private Long audioReferenceId;

    @Column(name = "pitch_score")
    private BigDecimal pitchScore;

    @Column(name = "rhythm_score")
    private BigDecimal rhythmScore;

    @Column(name = "tone_score")
    private BigDecimal toneScore;

    @Column(name = "overall_score")
    private BigDecimal overallScore;

    @Column(name = "stars_earned")
    private Long starsEarned;

    @Column(name = "audio_recording_url")
    private String audioRecordingUrl;

    @Column(name = "feedback")
    private String feedback;

    @Column(name = "created_at")
    private Date createdAt;

}
