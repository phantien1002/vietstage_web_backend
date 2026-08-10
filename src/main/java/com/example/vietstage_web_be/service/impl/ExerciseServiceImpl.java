package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.CreateExerciseRequest;
import com.example.vietstage_web_be.dto.request.UpdateExerciseRequest;
import com.example.vietstage_web_be.dto.response.ExerciseResponse;
import com.example.vietstage_web_be.entity.Exercise;
import com.example.vietstage_web_be.entity.MediaAsset;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.ExerciseRepository;
import com.example.vietstage_web_be.repository.MediaAssetRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.service.IExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import com.example.vietstage_web_be.entity.User;

@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements IExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final LessonRepository LessonRepository;
    private final MediaAssetRepository  MediaAssetRepository;

    @Override
    public List<ExerciseResponse> getExercisesByLesson(Long lessonId) {
        if (!LessonRepository.existsById(lessonId)) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }

        return exerciseRepository.findByLessonId(lessonId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void checkPermission(Lesson lesson, User instructor) {
        if ("ADMIN".equals(instructor.getRole().getName())) return;
        if (lesson.getCreatedBy() == null || !lesson.getCreatedBy().getId().equals(instructor.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_LESSON_ACCESS);
        }
    }

    private void shiftOrderIndexes(Long lessonId, int newOrderIndex) {
        List<Exercise> exercises = exerciseRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        boolean needsShift = false;
        for (Exercise ex : exercises) {
            if (ex.getOrderIndex() == newOrderIndex) {
                needsShift = true;
                break;
            }
        }
        if (needsShift) {
            int currentOrder = newOrderIndex;
            for (Exercise ex : exercises) {
                if (ex.getOrderIndex() >= currentOrder) {
                    ex.setOrderIndex(ex.getOrderIndex() + 1);
                    currentOrder = ex.getOrderIndex(); // keeping track of next shift
                    exerciseRepository.save(ex);
                }
            }
        }
    }

    @Override
    public ExerciseResponse createExercise(User instructor, Long lessonId, CreateExerciseRequest request) {
        Lesson lesson = LessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        checkPermission(lesson, instructor);
        shiftOrderIndexes(lessonId, request.getOrderIndex());

        Exercise exercise = new Exercise();

        exercise.setLesson(lesson);
        exercise.setTitle(request.getTitle());
        exercise.setDescription(request.getDescription());
        if (request.getPassThreshold() != null) {
            exercise.setPassThreshold(BigDecimal.valueOf(request.getPassThreshold()));
        }
        exercise.setOrderIndex(request.getOrderIndex());

        if (request.getBeatMapAssetId() != null) {
            MediaAsset asset =  MediaAssetRepository.findById(request.getBeatMapAssetId())
                    .orElseThrow(() -> new AppException(ErrorCode.BEAT_MAP_ASSET_NOT_FOUND));

            exercise.setBeatMapAsset(asset);
        }

        Exercise savedExercise = exerciseRepository.save(exercise);

        return mapToResponse(savedExercise);
    }

    @Override
    public ExerciseResponse updateExercise(User instructor, Long exerciseId, UpdateExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        checkPermission(exercise.getLesson(), instructor);

        if (exercise.getOrderIndex() != request.getOrderIndex()) {
             shiftOrderIndexes(exercise.getLesson().getId(), request.getOrderIndex());
        }

        exercise.setTitle(request.getTitle());
        exercise.setDescription(request.getDescription());
        if (request.getPassThreshold() != null) {
            exercise.setPassThreshold(BigDecimal.valueOf(request.getPassThreshold()));
        }
        exercise.setOrderIndex(request.getOrderIndex());

        if (request.getBeatMapAssetId() != null) {
            MediaAsset asset =  MediaAssetRepository.findById(request.getBeatMapAssetId())
                    .orElseThrow(() -> new AppException(ErrorCode.BEAT_MAP_ASSET_NOT_FOUND));

            exercise.setBeatMapAsset(asset);
        } else {
            exercise.setBeatMapAsset(null);
        }

        Exercise savedExercise = exerciseRepository.save(exercise);

        return mapToResponse(savedExercise);
    }

    @Override
    public void deleteExercise(User instructor, Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        checkPermission(exercise.getLesson(), instructor);

        exerciseRepository.delete(exercise);
    }

    private ExerciseResponse mapToResponse(Exercise exercise){
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

