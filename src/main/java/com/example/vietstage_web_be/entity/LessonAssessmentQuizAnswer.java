package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "lesson_assessment_quiz_answers", uniqueConstraints = @UniqueConstraint(
        name = "uk_assessment_session_quiz", columnNames = {"session_id", "quiz_id"}))
public class LessonAssessmentQuizAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id", nullable = false) private LessonAssessmentSession session;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "quiz_id", nullable = false) private Quiz quiz;
    @Column(name = "selected_answer", nullable = false) private String selectedAnswer;
    @Column(name = "is_correct", nullable = false) private Boolean correct;
    @Column(nullable = false, precision = 8, scale = 2) private BigDecimal score;
    @Column(name = "max_score", nullable = false, precision = 8, scale = 2) private BigDecimal maxScore;
}
