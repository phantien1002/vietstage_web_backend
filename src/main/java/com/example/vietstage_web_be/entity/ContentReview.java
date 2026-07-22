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
@Table(name = "content_reviews")
public class ContentReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "reviewer_user_id")
    private User reviewer;

    @Column(name = "status", nullable = false)
    private String status; // APPROVED | REJECTED

    @Column(name = "comment")
    private String comment;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
