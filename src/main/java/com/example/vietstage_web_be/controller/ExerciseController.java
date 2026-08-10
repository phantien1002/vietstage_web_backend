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

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Exercise", description = "Các API quản lý Bài tập")
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
    public ResponseEntity<ApiResponse<ExerciseResponse>> createExercise(
            @org.springframework.security.core.annotation.AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User instructor,
            @PathVariable Long id, 
            @Valid @RequestBody CreateExerciseRequest request){
        ExerciseResponse data = exerciseService.createExercise(instructor, id, request);
        return ResponseEntity.ok(ApiResponse.<ExerciseResponse>builder()
                .message("Create exercise successfully")
                .data(data)
                .build());
    }

    @PutMapping("/exercises/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<ExerciseResponse>> updateExercise(
            @org.springframework.security.core.annotation.AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User instructor,
            @PathVariable Long id, 
            @Valid @RequestBody UpdateExerciseRequest request){
        ExerciseResponse data = exerciseService.updateExercise(instructor, id, request);
        return ResponseEntity.ok(ApiResponse.<ExerciseResponse>builder()
                .message("Update exercise successfully")
                .data(data)
                .build());
    }

    @DeleteMapping("/exercises/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteExercise(
            @org.springframework.security.core.annotation.AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User instructor,
            @PathVariable Long id){
        exerciseService.deleteExercise(instructor, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Delete exercise successfully")
                .build());
    }
}

