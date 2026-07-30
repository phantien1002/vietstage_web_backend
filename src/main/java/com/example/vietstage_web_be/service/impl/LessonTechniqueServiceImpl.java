package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.LessonTechniqueRequest;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.Technique;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.TechniqueRepository;
import com.example.vietstage_web_be.service.ILessonTechniqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonTechniqueServiceImpl implements ILessonTechniqueService {

    private final LessonRepository lessonRepository;
    private final TechniqueRepository techniqueRepository;

    @Override
    @Transactional
    public void addTechnique(User instructor, Long lessonId, LessonTechniqueRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!lesson.getCreatedBy().getId().equals(instructor.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Technique technique = techniqueRepository.findById(request.getTechniqueId())
                .orElseThrow(() -> new AppException(ErrorCode.TECHNIQUE_NOT_FOUND));

        // Note: Assuming a basic instrument match check if required, skipping for simplicity here.
        // Actually we can just add to the Set.
        lesson.getTechniques().add(technique);
        lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public void removeTechnique(User instructor, Long lessonId, Long techniqueId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!lesson.getCreatedBy().getId().equals(instructor.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Technique technique = techniqueRepository.findById(techniqueId)
                .orElseThrow(() -> new AppException(ErrorCode.TECHNIQUE_NOT_FOUND));

        lesson.getTechniques().remove(technique);
        lessonRepository.save(lesson);
    }
}
