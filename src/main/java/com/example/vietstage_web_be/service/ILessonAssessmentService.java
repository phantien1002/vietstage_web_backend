package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.LessonAssessmentRequest;
import com.example.vietstage_web_be.dto.response.LessonAssessmentResponse;
import com.example.vietstage_web_be.entity.User;

public interface ILessonAssessmentService {
    LessonAssessmentResponse submit(Long lessonId, LessonAssessmentRequest request, User learner);
    LessonAssessmentResponse getDetail(Long sessionId, User learner);
}
