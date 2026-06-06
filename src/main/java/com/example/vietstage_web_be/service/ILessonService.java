package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.LessonRequest;
import com.example.vietstage_web_be.dto.response.LessonResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;

public interface ILessonService {
    LessonResponse createLesson(LessonRequest request, String userEmail);
    PageResponse<LessonResponse> getLessons(String search, Long instrumentId, String difficulty,
                                           int pageNumber, int pageSize, String sortBy, boolean sortDescending);
    LessonResponse getLessonById(Long id);
    LessonResponse updateLesson(Long id, LessonRequest request, String userEmail);
    void deleteLesson(Long id, String userEmail);
}
