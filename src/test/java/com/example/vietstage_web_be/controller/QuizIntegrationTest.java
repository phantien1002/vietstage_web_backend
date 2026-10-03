package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.QuizRequest;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.Quiz;
import com.example.vietstage_web_be.entity.Role;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.QuizRepository;
import com.example.vietstage_web_be.repository.RoleRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.open-in-view=false"
})
public class QuizIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User instructor;
    private Lesson lesson;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        quizRepository.deleteAll();
        lessonRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setName("INSTRUCTOR");
        role = roleRepository.save(role);

        instructor = User.builder()
                .username("testinstructor")
                .email("testinstructor@example.com")
                .password("password")
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
    }

    @AfterEach
    void tearDown() {
        quizRepository.deleteAll();
        lessonRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "testinstructor@example.com", authorities = {"ROLE_INSTRUCTOR"})
    void updateQuiz_success() throws Exception {
        QuizRequest request = new QuizRequest();
        request.setTitle("New Quiz Title");
        request.setQuestionType("MULTIPLE_CHOICE");
        request.setQuestion("What is 2+2?");
        request.setOptions("[\"1\", \"2\", \"3\", \"4\"]");
        request.setCorrectAnswer("4");
        request.setOrderIndex(1);
        request.setStatus("ACTIVE");

        mockMvc.perform(put("/api/quizzes/{id}", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Quiz Title"))
                .andExpect(jsonPath("$.question").value("What is 2+2?"));
    }
}
