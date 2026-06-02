package com.example.vietstage_web_be.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private Long id;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfiles userProfiles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserSessions> sessions;

    @OneToMany(mappedBy = "instructor", fetch = FetchType.LAZY)
    private List<Lessons> instructedLessons;

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PracticeAttempts>  practiceAttempts;

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LearnerProgress> progresses;

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MiniGameResults> miniGameResults;

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LearnerAchievements> learnerAchievements;

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LearnerDailyChallenges> learnerDailyChallenges;

    @OneToOne(mappedBy = "learner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PracticeStreaks practiceStreak;

    @OneToOne(mappedBy = "learner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Leaderboards leaderboard;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notifications> notifications;

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LearnerRewards> learnerRewards;
}
