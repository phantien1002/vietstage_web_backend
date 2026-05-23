package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "practice_attempts")
public class PracticeAttempts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private Users learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercises exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_reference_id")
    private AudioReferences audioReference;

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
    private LocalDateTime createdAt;

}
