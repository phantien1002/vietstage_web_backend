package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "skill_levels")
public class SkillLevels {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "level_name", nullable = false)
    private String levelName;

    @OneToMany(mappedBy = "skillLevel")
    private List<LearnerProfiles> learnerProfiles;
}
