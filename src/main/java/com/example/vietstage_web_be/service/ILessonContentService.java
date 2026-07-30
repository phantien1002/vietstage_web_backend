package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.LessonContentRequest;
import com.example.vietstage_web_be.dto.response.LessonContentResponse;
import com.example.vietstage_web_be.entity.User;

import java.util.List;

public interface ILessonContentService {
    List<LessonContentResponse> getLessonContents(Long lessonId);
    LessonContentResponse addContent(User instructor, Long lessonId, LessonContentRequest request);
    LessonContentResponse updateContent(User instructor, Long lessonId, Long contentId, LessonContentRequest request);
    void deleteContent(User instructor, Long lessonId, Long contentId);
}