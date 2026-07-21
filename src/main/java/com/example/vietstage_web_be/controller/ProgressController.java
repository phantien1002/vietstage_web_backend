package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.response.InstructorLearnerProgressResponse;
import com.example.vietstage_web_be.dto.response.LearnerProgressItemResponse;
import com.example.vietstage_web_be.dto.response.LearnerProgressSummaryResponse;
import com.example.vietstage_web_be.service.ILearnerProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProgressController {
    private final ILearnerProgressService progressService;

    @GetMapping("/users/me/progress")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<List<LearnerProgressItemResponse>> getLearnerProgress(
            @RequestParam(value = "instrument_id", required = false) Long instrumentId,
            @RequestParam(value = "skill_level_id", required = false) Long skillLevelId,
            Authentication authentication) {

        Long currentLearnerId = Long.parseLong(authentication.getName());

        List<LearnerProgressItemResponse> response = progressService.getLearnerProgress(
                currentLearnerId, instrumentId, skillLevelId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/me/progress/summary")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<LearnerProgressSummaryResponse> getLearnerProgressSummary(
            Authentication authentication) {

        Long currentLearnerId = Long.parseLong(authentication.getName());
        LearnerProgressSummaryResponse response = progressService.getLearnerProgressSummary(currentLearnerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/lessons/{id}/learners/{learner_id}/progress")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<InstructorLearnerProgressResponse> getLearnerProgressByInstructor(
            @PathVariable("id") Long lessonId,
            @PathVariable("learner_id") Long learnerId,
            Authentication authentication) {

        Long currentInstructorId = Long.parseLong(authentication.getName());
        InstructorLearnerProgressResponse response = progressService.getLearnerProgressByInstructor(
                lessonId, learnerId, currentInstructorId);

        return ResponseEntity.ok(response);
    }

}
