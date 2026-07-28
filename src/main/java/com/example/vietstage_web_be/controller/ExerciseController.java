package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.CreateExerciseRequest;
import com.example.vietstage_web_be.dto.request.UpdateExerciseRequest;
import com.example.vietstage_web_be.dto.response.ExerciseResponse;
import com.example.vietstage_web_be.service.IExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ExerciseController {
    private final IExerciseService exerciseService;

    @GetMapping("/lessons/{id}/exercises")
    public List<ExerciseResponse>
    getExercisesByLesson(@PathVariable Long id) {
        return exerciseService.getExercisesByLesson(id);
    }

    @PostMapping("/lessons/{id}/exercises")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ExerciseResponse createExercise(@PathVariable Long id, @Valid @RequestBody CreateExerciseRequest request){
        return exerciseService.createExercise(id,request);
    }

    @PutMapping("/exercises/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ExerciseResponse updateExercise(@PathVariable Long id, @Valid @RequestBody UpdateExerciseRequest request){
        return exerciseService.updateExercise(id,request);
    }

    @DeleteMapping("/exercises/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public void deleteExercise(@PathVariable Long id){
        exerciseService.deleteExercise(id);
    }
}
