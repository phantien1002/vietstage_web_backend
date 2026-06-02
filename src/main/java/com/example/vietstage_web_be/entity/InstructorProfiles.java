package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "instructor_profiles")
public class InstructorProfiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "biography")
    private String biography;

    @Column(name = "years_experience")
    private Long yearsExperience;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private Users user;
}
