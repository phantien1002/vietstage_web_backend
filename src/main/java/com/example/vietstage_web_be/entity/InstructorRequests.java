package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "instructor_requests")
public class InstructorRequests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "specialization", nullable = false)
    private String specialization;

    @Column(name = "biography")
    private String biography;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "status")
    private String status; // PENDING | APPROVED | REJECTED

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Users reviewer;

    @Column(name = "reviewer_note")
    private String reviewerNote;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
