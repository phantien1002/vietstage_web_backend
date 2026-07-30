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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "user_code", unique = true)
    private String userCode;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private InstructorProfile instructorProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private LearnerProfile learnerProfile;

    @OneToMany(mappedBy = "createdBy")
    private List<Lesson> createdLessons;

    @OneToMany(mappedBy = "learner")
    private List<PracticeAttempt> PracticeAttempt;

    @OneToMany(mappedBy = "instructor")
    private List<InstructorFeedback> givenFeedbacks;

    @OneToMany(mappedBy = "learner")
    private List<LearnerAchievement> learnerAchievements;

    @OneToMany(mappedBy = "learner")
    private List<LearnerCosmetic> learnerCosmetics;

    @OneToMany(mappedBy = "user")
    private List<PointTransaction> pointTransactions;

    @OneToMany(mappedBy = "learner")
    private List<PracticeSession> practiceSessions;

    @OneToMany(mappedBy = "learner")
    private List<PracticeAttempt> practiceAttempts;

    @OneToMany(mappedBy = "instructor")
    private List<InstructorFeedback> instructorFeedbacks;

    @ManyToMany
    @JoinTable(
            name = "learner_daily_challenges",
            joinColumns = @JoinColumn(name = "learner_user_id"),
            inverseJoinColumns = @JoinColumn(name = "challenge_id")
    )
    private Set<DailyChallenge> dailyChallenges;

    @OneToMany(mappedBy = "learner")
    private List<MinigameAttempt> minigameAttempts;
    
    @OneToMany(mappedBy = "learner")
    private List<QuizAttempt> QuizAttempt;
}

