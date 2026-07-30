package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.LessonContentRequest;
import com.example.vietstage_web_be.dto.response.LessonContentResponse;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.LessonContent;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.LessonContentRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.service.ILessonContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonContentServiceImpl implements ILessonContentService {

    private final LessonContentRepository contentRepository;
    private final LessonRepository lessonRepository;

    @Override
    public List<LessonContentResponse> getLessonContents(Long lessonId) {
        return contentRepository.findByLessonIdOrderByOrderIndexAsc(lessonId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public LessonContentResponse addContent(User instructor, Long lessonId, LessonContentRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!lesson.getCreatedBy().getId().equals(instructor.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        LessonContent content = LessonContent.builder()
                .lesson(lesson)
                .contentText(request.getContentText())
                .orderIndex(request.getOrderIndex())
                .build();
        
        contentRepository.save(content);
        return mapToResponse(content);
    }

    @Override
    @Transactional
    public LessonContentResponse updateContent(User instructor, Long lessonId, Long contentId, LessonContentRequest request) {
        LessonContent content = validateOwnership(instructor, lessonId, contentId);

        content.setContentText(request.getContentText());
        content.setOrderIndex(request.getOrderIndex());
        
        contentRepository.save(content);
        return mapToResponse(content);
    }

    @Override
    @Transactional
    public void deleteContent(User instructor, Long lessonId, Long contentId) {
        LessonContent content = validateOwnership(instructor, lessonId, contentId);
        contentRepository.delete(content);
    }

    private LessonContent validateOwnership(User instructor, Long lessonId, Long contentId) {
        LessonContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!content.getLesson().getId().equals(lessonId)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (!content.getLesson().getCreatedBy().getId().equals(instructor.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        return content;
    }

    private LessonContentResponse mapToResponse(LessonContent content) {
        return LessonContentResponse.builder()
                .id(content.getId())
                .contentText(content.getContentText())
                .orderIndex(content.getOrderIndex())
                .build();
    }
}
