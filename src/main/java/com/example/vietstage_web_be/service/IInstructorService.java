package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.InstructorPracticeAttemptRequest;
import com.example.vietstage_web_be.dto.response.LearnerSummaryResponse;
import com.example.vietstage_web_be.dto.response.PracticeAttemptDetailResponse;
import com.example.vietstage_web_be.dto.response.PracticeAttemptGroupedResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IInstructorService {
    LearnerSummaryResponse getLearnerProgressSummary(Long learnerId);

    Page<PracticeAttemptDetailResponse> getFilteredPracticeAttemptDetail(InstructorPracticeAttemptRequest request);

    List<PracticeAttemptGroupedResponse> getGroupedPracticeAttemptDetail(PracticeAttemptGroupedResponse request);
}
