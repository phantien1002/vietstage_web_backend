package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lessons")
public class Lessons {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instrument_id")
    private Instruments instrument;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "difficulty")
    private String difficulty;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @ManyToMany
    @JoinTable(
            name = "lesson_techniques",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "technique_id")
    )
    private Set<Techniques> techniques;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonContents> lessonContents;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AudioReferences> audioReferences;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exercises> exercises;
}
