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

import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "audio_references")
public class AudioReferences {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Column(name = "bpm")
    private Long bpm;

    @Column(name = "key_signature")
    private String keySignature;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "created_at")
    private Date createdAt;

}
