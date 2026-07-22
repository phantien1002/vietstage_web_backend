package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "skill_levels")
public class SkillLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_level_id", nullable = false)
    private Long id;

    @Column(name = "level_code", nullable = false, unique = true, length = 20)
    private String levelCode;

    @Column(name = "level_name", nullable = false, length = 50)
    private String levelName;

    @Column(name = "order_index", nullable = false, unique = true)
    private Short orderIndex;
}
