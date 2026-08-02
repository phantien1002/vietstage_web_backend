package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.InstructorLearnerProgressResponse;
import com.example.vietstage_web_be.dto.response.LearnerProgressItemResponse;
import com.example.vietstage_web_be.dto.response.LearnerProgressSummaryResponse;
import com.example.vietstage_web_be.entity.LearnerProfile;

import java.util.List;

public interface ILearnerProgressService {
    LearnerProfile updateStreakAndSave(LearnerProfile profile);

    // API 54: /api/users/me/progress
    List<LearnerProgressItemResponse> getLearnerProgress(Long learnerId, Long instrumentId, Long skillLevelId);

    // API 55: /api/users/me/progress/summary
    LearnerProgressSummaryResponse getLearnerProgressSummary(Long learnerId);

    // API 56: /api/Lesson/{id}/learners/{learner_id}/progress
    InstructorLearnerProgressResponse getLearnerProgressByInstructor(Long lessonId, Long learnerId, Long instructorId);
}

