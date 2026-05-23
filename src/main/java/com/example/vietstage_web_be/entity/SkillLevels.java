package com.example.vietstage_web_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "skill_levels")
public class SkillLevels {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "level_name", nullable = false)
    private String levelName;

    @Column(name = "description")
    private String description;

}
