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
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private InstructorProfiles instructorProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private LearnerProfiles learnerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Leaderboards leaderboard;

    @OneToMany(mappedBy = "createdBy")
    private List<Lessons> createdLessons;

    @OneToMany(mappedBy = "learner")
    private List<PracticeAttempts> practiceAttempts;

    @OneToMany(mappedBy = "instructor")
    private List<InstructorFeedback> givenFeedbacks;

    @ManyToMany
    @JoinTable(
            name = "learner_achievements",
            joinColumns = @JoinColumn(name = "learner_id"),
            inverseJoinColumns = @JoinColumn(name = "achievement_id")
    )
    private Set<Achievements> achievements;

    @ManyToMany
    @JoinTable(
            name = "learner_daily_challenges",
            joinColumns = @JoinColumn(name = "learner_id"),
            inverseJoinColumns = @JoinColumn(name = "challenge_id")
    )
    private Set<DailyChallenges> dailyChallenges;

    @OneToMany(mappedBy = "learner")
    private List<MiniGameResults> miniGameResults;
}
