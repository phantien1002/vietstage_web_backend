package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audio_references")
public class AudioReferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lessons lesson;

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

}
