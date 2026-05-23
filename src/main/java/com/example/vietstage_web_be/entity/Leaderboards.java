package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "leaderboards")
public class Leaderboards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", referencedColumnName = "id", nullable = false, unique = true)
    private Users learner;

    @Column(name = "total_points")
    private Long totalPoints;

    @Column(name = "rank_position")
    private Long rankPosition;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
