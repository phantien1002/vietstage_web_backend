package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "instructor_profiles")
public class InstructorProfile {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @Column(name = "years_experience", nullable = false)
    private Integer yearsExperience = 0;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "instructor_instruments",
            joinColumns = @JoinColumn(name = "instructor_user_id"),
            inverseJoinColumns = @JoinColumn(name = "instrument_id")
    )
    private Set<Instrument> instruments;
}
