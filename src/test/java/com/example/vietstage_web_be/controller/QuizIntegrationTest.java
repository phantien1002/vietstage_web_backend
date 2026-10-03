package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.QuizRequest;
import com.example.vietstage_web_be.dto.response.QuizResponse;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.Quiz;
import com.example.vietstage_web_be.entity.Role;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.QuizRepository;
import com.example.vietstage_web_be.repository.RoleRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.repository.AuditLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.open-in-view=false"
})
public class QuizIntegrationTest {

    @Autowired
    private QuizController quizController;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private User instructor;
    private Lesson lesson;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        quizRepository.deleteAll();
        lessonRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setName("INSTRUCTOR");
        role = roleRepository.save(role);

        instructor = User.builder()
                .fullName("testinstructor")
                .email("testinstructor@example.com")
                .passwordHash("password")
                .role(role)
                .build();
        instructor = userRepository.save(instructor);

        lesson = Lesson.builder()
                .title("Test Lesson")
                .createdBy(instructor)
                .build();
        lesson = lessonRepository.save(lesson);

        quiz = Quiz.builder()
                .lesson(lesson)
                .title("Old Quiz Title")
                .questionType("MULTIPLE_CHOICE")
                .question("What is 1+1?")
                .options("[\"1\", \"2\", \"3\", \"4\"]")
                .correctAnswer("2")
                .orderIndex(1)
                .status("ACTIVE")
                .build();
        quiz = quizRepository.save(quiz);

        // Mock Security Context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(instructor, null, null)
        );
    }

    @AfterEach
    void tearDown() {
        quizRepository.deleteAll();
        lessonRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateQuiz_success() {
        QuizRequest request = new QuizRequest();
        request.setTitle("New Quiz Title");
        request.setQuestionType("MULTIPLE_CHOICE");
        request.setQuestion("What is 2+2?");
        request.setOptions("[\"1\", \"2\", \"3\", \"4\"]");
        request.setCorrectAnswer("4");
        request.setOrderIndex(1);
        request.setStatus("ACTIVE");

        ResponseEntity<BaseResponse<QuizResponse>> response = quizController.updateQuiz(quiz.getId(), request, instructor);
        
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("New Quiz Title", response.getBody().getData().getTitle());
        assertEquals("What is 2+2?", response.getBody().getData().getQuestion());
    }
}
