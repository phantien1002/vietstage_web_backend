package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.LessonTechniqueRequest;
import com.example.vietstage_web_be.entity.User;

public interface ILessonTechniqueService {
    void addTechnique(User instructor, Long lessonId, LessonTechniqueRequest request);
    void removeTechnique(User instructor, Long lessonId, Long techniqueId);
}