package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.CreateExerciseRequest;
import com.example.vietstage_web_be.dto.request.UpdateExerciseRequest;
import com.example.vietstage_web_be.dto.response.ExerciseResponse;
import com.example.vietstage_web_be.entity.Exercises;

import java.util.List;

public interface IExerciseService {
    List<ExerciseResponse> getExercisesByLesson(Long lessonId);

    ExerciseResponse createExercise(Long lessonId, CreateExerciseRequest request);

    ExerciseResponse updateExercise(Long exerciseId, UpdateExerciseRequest request);

    void deleteExercise(Long exerciseId);
}
