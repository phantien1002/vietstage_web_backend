package com.example.vietstage_web_be.entity;

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
@Table(name = "practice_sessions")
public class PracticeSessions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "learner_id")
    private Users learner;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "sync_status")
    private String syncStatus; // SYNCED | PENDING_SYNC | CONFLICT

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<PracticeAttempts> attempts;
}
