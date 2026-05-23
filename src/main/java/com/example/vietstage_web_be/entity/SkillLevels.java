package com.example.vietstage_web_be.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Long id;

    @Column(name = "level_name", nullable = false)
    private String levelName;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "skillLevel", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Lessons> lessons;
}
