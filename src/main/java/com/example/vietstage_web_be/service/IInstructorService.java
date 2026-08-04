package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.InstructorPracticeAttemptRequest;
import com.example.vietstage_web_be.dto.response.LearnerProgressSummaryResponse;
import com.example.vietstage_web_be.dto.response.PracticeAttemptDetailResponse;
import com.example.vietstage_web_be.dto.response.PracticeAttemptGroupedResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IInstructorService {
    LearnerProgressSummaryResponse getLearnerProgressSummary(Long learnerId);

    Page<PracticeAttemptDetailResponse> getFilteredPracticeAttempts(InstructorPracticeAttemptRequest request);

    List<PracticeAttemptGroupedResponse> getGroupedPracticeAttemptDetail(InstructorPracticeAttemptRequest request);
}
