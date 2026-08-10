package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.CreateExerciseRequest;
import com.example.vietstage_web_be.dto.request.UpdateExerciseRequest;
import com.example.vietstage_web_be.dto.response.ExerciseResponse;
import com.example.vietstage_web_be.entity.Exercise;

import com.example.vietstage_web_be.entity.User;
import java.util.List;

public interface IExerciseService {
    List<ExerciseResponse> getExercisesByLesson(Long lessonId);

    ExerciseResponse createExercise(User instructor, Long lessonId, CreateExerciseRequest request);

    ExerciseResponse updateExercise(User instructor, Long exerciseId, UpdateExerciseRequest request);

    void deleteExercise(User instructor, Long exerciseId);
}

