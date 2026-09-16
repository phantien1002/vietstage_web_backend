package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id", nullable = false)
    private Long id;

    @Column(name = "lesson_code", unique = true, nullable = false)
    private String lessonCode;

    @ManyToOne
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    @ManyToOne
    @JoinColumn(name = "skill_level_id")
    private SkillLevel skillLevel;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "technical_notes", columnDefinition = "TEXT")
    private String technicalNotes;

    // --- New Fields based on Handoff ---

    @Column(name = "display_number", nullable = false)
    private String displayNumber = ""; // Default empty if missing

    @Column(name = "legacy_level", nullable = false)
    private Integer legacyLevel = 0; // Default 0

    @Column(name = "in_current_roadmap", nullable = false)
    private Boolean inCurrentRoadmap = false;

    @Column(name = "approval_status", nullable = false)
    private String approvalStatus = "DRAFT";

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = false;

    @Column(name = "hidden_at")
    private LocalDateTime hiddenAt;

    @Column(name = "hidden_reason")
    private String hiddenReason;

    @Column(name = "revision", nullable = false)
    private Integer revision = 1;

    // Store raw JSON as TEXT or JSONB. Using jsonb for PostgreSQL.
    @Column(name = "source_snapshot", columnDefinition = "jsonb")
    private String sourceSnapshot;

    // -----------------------------------

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "lesson_techniques",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "technique_id")
    )
    private Set<Technique> techniques;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<LessonContent> lessonContents;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<Exercise> exercises;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<Quiz> quizzes;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<MinigameChallenge> minigameChallenges;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<MediaAsset> mediaAssets;
}
