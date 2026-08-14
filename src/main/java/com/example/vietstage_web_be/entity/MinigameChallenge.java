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
@Table(name = "minigame_challenges")
public class MinigameChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    @Column(name = "reference_asset_id")
    private Long referenceAssetId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "challenge_type", nullable = false)
    private String challengeType;

    @Column(name = "content_json")
    private String contentJson;

    @Column(name = "difficulty")
    private String difficulty;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private List<MinigameAttempt> attempts;
}
