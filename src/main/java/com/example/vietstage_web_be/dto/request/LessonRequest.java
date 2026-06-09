package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class LessonRequest {
    @NotBlank(message = "Lesson title cannot be blank")
    private String title;

    @NotBlank(message = "Lesson difficulty cannot be blank")
    private String difficulty;

    @NotNull(message = "Instrument ID cannot be null")
    private Long instrumentId;

    private Set<Long> techniqueIds;

    private List<String> contents;

    private List<String> audioUrls;

    private List<String> exercises;
}
