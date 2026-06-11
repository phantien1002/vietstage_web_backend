package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "daily_challenges")
public class DailyChallenges {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "instrument_id")
    private Instruments instrument;

    @Column(name = "reward_points")
    private Integer rewardPoints;

    @Column(name = "challenge_date")
    private LocalDate challengeDate;

    @ManyToMany(mappedBy = "dailyChallenges")
    private Set<Users> learners;
}
