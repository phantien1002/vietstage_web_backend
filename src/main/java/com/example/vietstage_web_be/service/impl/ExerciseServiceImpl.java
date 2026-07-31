package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.CreateExerciseRequest;
import com.example.vietstage_web_be.dto.request.UpdateExerciseRequest;
import com.example.vietstage_web_be.dto.response.ExerciseResponse;
import com.example.vietstage_web_be.entity.Exercise;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.ExerciseRepository;
import com.example.vietstage_web_be.repository.LessonAssetsRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.service.IExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements IExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final LessonRepository lessonsRepository;
    private final LessonAssetsRepository  lessonAssetsRepository;

    @Override
    public List<ExerciseResponse> getExercisesByLesson(Long lessonId) {
        if (lessonsRepository.existsById(lessonId)) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }

        return exerciseRepository.findByLessonId(lessonId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ExerciseResponse createExercise(Long lessonId, CreateExerciseRequest request) {
        Lesson lesson = lessonsRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        Exercise exercise = new Exercise();

        exercise.setLesson(lesson);
        exercise.setTitle(request.getTitle());
        exercise.setDescription(request.getDescription());
        exercise.setPassThreshold(BigDecimal.valueOf(request.getPassThreshold()));
        exercise.setOrderIndex(request.getOrderIndex());

        if (request.getPassThreshold() != null) {
            LessonAsset asset =  lessonAssetsRepository.findById(request.getBeatMapAssetId())
                    .orElseThrow(() -> new AppException(ErrorCode.BEAT_MAP_ASSET_NOT_FOUND));

            exercise.setBeatMapAsset(asset);
        }

        Exercise savedExercise = exerciseRepository.save(exercise);

        return mapToResponse(savedExercise);
    }

    @Override
    public ExerciseResponse updateExercise(Long exerciseId, UpdateExerciseRequest request) {
        Exercises exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        exercise.setTitle(request.getTitle());
        exercise.setDescription(request.getDescription());
        exercise.setPassThreshold(BigDecimal.valueOf(request.getPassThreshold()));
        exercise.setOrderIndex(request.getOrderIndex());

        if (request.getBeatMapAssetId() != null) {
            LessonAssets asset =  lessonAssetsRepository.findById(request.getBeatMapAssetId())
                    .orElseThrow(() -> new AppException(ErrorCode.BEAT_MAP_ASSET_NOT_FOUND));

            exercise.setBeatMapAsset(asset);
        }

        Exercises savedExercise = exerciseRepository.save(exercise);

        return mapToResponse(savedExercise);
    }

    @Override
    public void deleteExercise(Long exerciseId) {
        Exercises exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        exerciseRepository.delete(exercise);
    }

    private ExerciseResponse mapToResponse(Exercises exercise){
        return ExerciseResponse.builder()
                .id(exercise.getId())
                .lessonId(
                        exercise.getLesson().getId())
                .title(exercise.getTitle())
                .description(
                        exercise.getDescription())
                .beatMapAssetId(

                        exercise.getBeatMapAsset()==null
                                ?null
                                :exercise.getBeatMapAsset().getId()
                )
                .passThreshold(
                        exercise.getPassThreshold() == null
                                ? null
                                : exercise.getPassThreshold().doubleValue()
                )
                .orderIndex(
                        exercise.getOrderIndex())
                .build();

    }
}
