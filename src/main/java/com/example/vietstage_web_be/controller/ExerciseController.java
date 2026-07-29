package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.CreateExerciseRequest;
import com.example.vietstage_web_be.dto.request.UpdateExerciseRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.ExerciseResponse;
import com.example.vietstage_web_be.service.IExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ExerciseController {
    private final IExerciseService exerciseService;

    @GetMapping("/lessons/{id}/exercises")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getExercisesByLesson(@PathVariable Long id) {
        List<ExerciseResponse> data = exerciseService.getExercisesByLesson(id);
        return ResponseEntity.ok(ApiResponse.<List<ExerciseResponse>>builder()
                .message("Get Exercise successfully")
                .data(data)
                .build());
    }

    @PostMapping("/lessons/{id}/exercises")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<ExerciseResponse>> createExercise(@PathVariable Long id, @Valid @RequestBody CreateExerciseRequest request){
        ExerciseResponse data = exerciseService.createExercise(id, request);
        return ResponseEntity.ok(ApiResponse.<ExerciseResponse>builder()
                .message("Create exercise successfully")
                .data(data)
                .build());
    }

    @PutMapping("/exercises/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<ExerciseResponse>> updateExercise(@PathVariable Long id, @Valid @RequestBody UpdateExerciseRequest request){
        ExerciseResponse data = exerciseService.updateExercise(id, request);
        return ResponseEntity.ok(ApiResponse.<ExerciseResponse>builder()
                .message("Update exercise successfully")
                .data(data)
                .build());
    }

    @DeleteMapping("/exercises/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteExercise(@PathVariable Long id){
        exerciseService.deleteExercise(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Delete exercise successfully")
                .build());
    }
}

