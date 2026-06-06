package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.LessonRequest;
import com.example.vietstage_web_be.dto.response.LessonResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.entity.*;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.*;
import com.example.vietstage_web_be.service.ILessonService;
import com.example.vietstage_web_be.specification.LessonSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements ILessonService {

    private final LessonsRepository lessonsRepository;
    private final InstrumentsRepository instrumentsRepository;
    private final UsersRepository usersRepository;
    private final TechniquesRepository techniquesRepository;

    @Override
    @Transactional
    public LessonResponse createLesson(LessonRequest request, String userEmail) {
        Instruments instrument = instrumentsRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));

        Users creator = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Set<Techniques> techniques = new HashSet<>();
        if (request.getTechniqueIds() != null && !request.getTechniqueIds().isEmpty()) {
            techniques.addAll(techniquesRepository.findAllById(request.getTechniqueIds()));
        }

        // Tạo đối tượng Lessons cơ bản
        Lessons lesson = Lessons.builder()
                .title(request.getTitle())
                .difficulty(request.getDifficulty())
                .instrument(instrument)
                .createdBy(creator)
                .techniques(techniques)
                .lessonContents(new ArrayList<>())
                .audioReferences(new ArrayList<>())
                .exercises(new ArrayList<>())
                .build();

        // Gán các thông tin nội dung đi kèm
        if (request.getContents() != null) {
            List<LessonContents> contents = request.getContents().stream()
                    .map(text -> LessonContents.builder()
                            .lesson(lesson)
                            .contentText(text)
                            .build())
                    .collect(Collectors.toList());
            lesson.getLessonContents().addAll(contents);
        }

        if (request.getAudioUrls() != null) {
            List<AudioReferences> audios = request.getAudioUrls().stream()
                    .map(url -> AudioReferences.builder()
                            .lesson(lesson)
                            .audioUrl(url)
                            .build())
                    .collect(Collectors.toList());
            lesson.getAudioReferences().addAll(audios);
        }

        if (request.getExercises() != null) {
            List<Exercises> exercises = request.getExercises().stream()
                    .map(title -> Exercises.builder()
                            .lesson(lesson)
                            .title(title)
                            .build())
                    .collect(Collectors.toList());
            lesson.getExercises().addAll(exercises);
        }

        Lessons saved = lessonsRepository.save(lesson);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LessonResponse> getLessons(String search, Long instrumentId, String difficulty,
                                                   int pageNumber, int pageSize, String sortBy, boolean sortDescending) {
        // Đảm bảo pageNumber tối thiểu là 1
        int zeroBasedPage = Math.max(pageNumber - 1, 0);

        // Giới hạn pageSize tối đa 100
        int size = Math.min(Math.max(pageSize, 1), 100);

        // Xử lý Sort
        String validSortBy = "id";
        if ("title".equalsIgnoreCase(sortBy) || "difficulty".equalsIgnoreCase(sortBy)) {
            validSortBy = sortBy;
        }
        Sort sort = sortDescending ? Sort.by(validSortBy).descending() : Sort.by(validSortBy).ascending();

        Pageable pageable = PageRequest.of(zeroBasedPage, size, sort);
        Specification<Lessons> spec = LessonSpecification.filter(search, instrumentId, difficulty);

        Page<Lessons> lessonsPage = lessonsRepository.findAll(spec, pageable);

        List<LessonResponse> responses = lessonsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<LessonResponse>builder()
                .page(lessonsPage.getNumber() + 1)
                .size(lessonsPage.getSize())
                .totalElements(lessonsPage.getTotalElements())
                .totalPages(lessonsPage.getTotalPages())
                .content(responses)
                .last(lessonsPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponse getLessonById(Long id) {
        Lessons lesson = lessonsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        return mapToResponse(lesson);
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(Long id, LessonRequest request, String userEmail) {
        Lessons lesson = lessonsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        // Kiểm tra quyền sở hữu
        checkLessonPermission(lesson, userEmail);

        Instruments instrument = instrumentsRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));

        Set<Techniques> techniques = new HashSet<>();
        if (request.getTechniqueIds() != null && !request.getTechniqueIds().isEmpty()) {
            techniques.addAll(techniquesRepository.findAllById(request.getTechniqueIds()));
        }

        // Cập nhật thông tin cơ bản
        lesson.setTitle(request.getTitle());
        lesson.setDifficulty(request.getDifficulty());
        lesson.setInstrument(instrument);
        lesson.setTechniques(techniques);

        // Cập nhật các danh sách liên quan (Nhờ orphanRemoval = true)
        lesson.getLessonContents().clear();
        if (request.getContents() != null) {
            List<LessonContents> contents = request.getContents().stream()
                    .map(text -> LessonContents.builder()
                            .lesson(lesson)
                            .contentText(text)
                            .build())
                    .collect(Collectors.toList());
            lesson.getLessonContents().addAll(contents);
        }

        lesson.getAudioReferences().clear();
        if (request.getAudioUrls() != null) {
            List<AudioReferences> audios = request.getAudioUrls().stream()
                    .map(url -> AudioReferences.builder()
                            .lesson(lesson)
                            .audioUrl(url)
                            .build())
                    .collect(Collectors.toList());
            lesson.getAudioReferences().addAll(audios);
        }

        lesson.getExercises().clear();
        if (request.getExercises() != null) {
            List<Exercises> exercises = request.getExercises().stream()
                    .map(title -> Exercises.builder()
                            .lesson(lesson)
                            .title(title)
                            .build())
                    .collect(Collectors.toList());
            lesson.getExercises().addAll(exercises);
        }

        Lessons updated = lessonsRepository.save(lesson);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id, String userEmail) {
        Lessons lesson = lessonsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        // Kiểm tra quyền sở hữu
        checkLessonPermission(lesson, userEmail);

        lessonsRepository.delete(lesson);
    }

    private void checkLessonPermission(Lessons lesson, String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Admin có toàn quyền
        if ("ADMIN".equals(user.getRole())) {
            return;
        }

        // Instructor chỉ được sửa/xóa bài học của chính họ
        if (lesson.getCreatedBy() == null || !lesson.getCreatedBy().getEmail().equals(userEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_LESSON_ACCESS);
        }
    }

    private LessonResponse mapToResponse(Lessons lesson) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .difficulty(lesson.getDifficulty())
                .instrument(lesson.getInstrument() != null ? LessonResponse.InstrumentInfo.builder()
                        .id(lesson.getInstrument().getId())
                        .name(lesson.getInstrument().getName())
                        .build() : null)
                .createdBy(lesson.getCreatedBy() != null ? LessonResponse.CreatorInfo.builder()
                        .id(lesson.getCreatedBy().getId())
                        .fullName(lesson.getCreatedBy().getFullName())
                        .role(lesson.getCreatedBy().getRole())
                        .build() : null)
                .techniques(lesson.getTechniques() != null ? lesson.getTechniques().stream()
                        .map(t -> LessonResponse.TechniqueInfo.builder()
                                .id(t.getId())
                                .name(t.getName())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .contents(lesson.getLessonContents() != null ? lesson.getLessonContents().stream()
                        .map(c -> LessonResponse.ContentInfo.builder()
                                .id(c.getId())
                                .contentText(c.getContentText())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .audioReferences(lesson.getAudioReferences() != null ? lesson.getAudioReferences().stream()
                        .map(a -> LessonResponse.AudioInfo.builder()
                                .id(a.getId())
                                .audioUrl(a.getAudioUrl())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .exercises(lesson.getExercises() != null ? lesson.getExercises().stream()
                        .map(e -> LessonResponse.ExerciseInfo.builder()
                                .id(e.getId())
                                .title(e.getTitle())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
}
