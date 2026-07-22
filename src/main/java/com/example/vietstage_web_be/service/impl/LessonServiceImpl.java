package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.LessonRequest;
import com.example.vietstage_web_be.dto.request.LessonStatusRequest;
import com.example.vietstage_web_be.dto.request.UpdateLessonRequest;
import com.example.vietstage_web_be.dto.response.LessonResponse;
import com.example.vietstage_web_be.dto.response.LessonStatusResponse;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements ILessonService {

    private static final List<String> VALID_STATUSES = List.of("DRAFT", "PENDING", "APPROVED", "REJECTED");

    private final LessonRepository lessonRepository;
    private final InstrumentRepository instrumentRepository;
    private final UserRepository userRepository;
    private final TechniqueRepository techniqueRepository;
    private final SkillLevelRepository skillLevelRepository;
    private final ContentReviewRepository contentReviewRepository;

    // =========================================================
    // POST /api/lessons
    // =========================================================
    @Override
    @Transactional
    public LessonResponse createLesson(LessonRequest request, String userEmail) {
        Instrument instrument = instrumentRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new AppException(ErrorCode.INSTRUMENT_NOT_FOUND));

        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (lessonRepository.existsByTitleIgnoreCaseAndInstrumentId(request.getTitle(), request.getInstrumentId())) {
            throw new AppException(ErrorCode.LESSON_ALREADY_EXIST);
        }

        // v2.0: skill_level_id thay thế difficulty string
        SkillLevel skillLevel = null;
        if (request.getSkillLevelId() != null) {
            skillLevel = skillLevelRepository.findById(request.getSkillLevelId()).orElse(null);
        }

        Set<Technique> techniques = new HashSet<>();
        if (request.getTechniqueIds() != null && !request.getTechniqueIds().isEmpty()) {
            techniques.addAll(techniqueRepository.findAllById(request.getTechniqueIds()));
        }

        // Auto-generate lesson code
        String insCode = instrument.getInstrumentCode() != null ? instrument.getInstrumentCode() : "INS";
        String lvlCode = skillLevel != null && skillLevel.getLevelCode() != null ? skillLevel.getLevelCode() : "U";
        int order = request.getOrderIndex() != null ? request.getOrderIndex() : 0;
        Long nextId = lessonRepository.findTopByOrderByIdDesc().map(Lesson::getId).orElse(0L) + 1;
        String generatedLessonCode = String.format("LSN-%s-%s-%03d", insCode.replace("INS-", ""), lvlCode, nextId);

        Lesson lesson = Lesson.builder()
                .lessonCode(generatedLessonCode)
                .title(request.getTitle())
                .description(request.getDescription())
                .status("DRAFT")                            // Spec: POST luôn tạo với status=DRAFT
                .orderIndex(order)
                .skillLevel(skillLevel)
                .instrument(instrument)
                .createdBy(creator)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .techniques(techniques)
                .exercises(new ArrayList<>())
                .mediaAssets(new ArrayList<>())
                .build();

        if (request.getAssets() != null) {
            List<MediaAsset> assets = request.getAssets().stream().map(a -> MediaAsset.builder()
                    .lesson(lesson)
                    .assetType(a.getAssetType())
                    .assetUrl(a.getAssetUrl())
                    .tempoBpm(a.getTempoBpm())
                    .durationSec(a.getDurationSec())
                    .createdAt(LocalDateTime.now())
                    .build()).collect(Collectors.toList());
            lesson.getMediaAssets().addAll(assets);
        }

        if (request.getExercises() != null) {
            List<Exercise> exercises = new ArrayList<>();
            for (int i = 0; i < request.getExercises().size(); i++) {
                exercises.add(Exercise.builder()
                        .lesson(lesson)
                        .title(request.getExercises().get(i))
                        .passThreshold(new java.math.BigDecimal("60.00"))
                        .orderIndex(i + 1)
                        .build());
            }
            lesson.getExercises().addAll(exercises);
        }

        Lesson saved = lessonRepository.save(lesson);
        return mapToResponse(saved);
    }

    // =========================================================
    // GET /api/lessons
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponse<LessonResponse> getLessons(String search, Long instrumentId, Long skillLevelId,
                                                   String status,
                                                   int pageNumber, int pageSize) {
        int zeroBasedPage = Math.max(pageNumber - 1, 0);
        int size = Math.min(Math.max(pageSize, 1), 100);

        Pageable pageable = PageRequest.of(zeroBasedPage, size, Sort.by("orderIndex").ascending());
        Specification<Lesson> spec = LessonSpecification.filter(search, instrumentId, skillLevelId, status);

        Page<Lesson> lessonsPage = lessonRepository.findAll(spec, pageable);

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

    // =========================================================
    // GET /api/lessons/{id}
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public LessonResponse getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        return mapToResponse(lesson);
    }

    // =========================================================
    // PUT /api/lessons/{id}
    // Spec: chỉ cập nhật title, description, order_index, skill_level_id
    // instrument_id KHÔNG thay đổi sau khi tạo
    // =========================================================
    @Override
    @Transactional
    public LessonResponse updateLesson(Long id, UpdateLessonRequest request, String userEmail) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        checkLessonPermission(lesson, userEmail);

        // Check trùng title trong cùng nhạc cụ (nếu title thay đổi)
        if (!lesson.getTitle().equalsIgnoreCase(request.getTitle())) {
            Long instrumentId = lesson.getInstrument() != null ? lesson.getInstrument().getId() : null;
            if (instrumentId != null &&
                    lessonRepository.existsByTitleIgnoreCaseAndInstrumentId(request.getTitle(), instrumentId)) {
                throw new AppException(ErrorCode.LESSON_ALREADY_EXIST);
            }
        }

        // v2.0: skill_level_id
        SkillLevel skillLevel = null;
        if (request.getSkillLevelId() != null) {
            skillLevel = skillLevelRepository.findById(request.getSkillLevelId()).orElse(null);
        }

        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        }
        lesson.setSkillLevel(skillLevel);
        lesson.setUpdatedAt(LocalDateTime.now());

        Lesson updated = lessonRepository.save(lesson);
        return mapToResponse(updated);
    }

    // =========================================================
    // DELETE /api/lessons/{id}
    // =========================================================
    @Override
    @Transactional
    public void deleteLesson(Long id, String userEmail) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        checkLessonPermission(lesson, userEmail);

        lessonRepository.delete(lesson);
    }

    // =========================================================
    // PUT /api/lessons/{id}/status
    // - INSTRUCTOR: chỉ được đặt PENDING (nộp bài duyệt)
    // - ADMIN: được đặt APPROVED hoặc REJECTED (kèm comment)
    // Sau khi thay đổi: ghi content_reviews + gửi notification cho người tạo
    // =========================================================
    @Override
    @Transactional
    public LessonStatusResponse updateLessonStatus(Long id, LessonStatusRequest request, String userEmail) {
        // Validate status value
        String newStatus = request.getStatus().toUpperCase();
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new AppException(ErrorCode.INVALID_LESSON_STATUS);
        }

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        User actor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isAdmin = "ADMIN".equals(actor.getRole().getName());
        boolean isInstructor = "INSTRUCTOR".equals(actor.getRole().getName());

        // INSTRUCTOR: chỉ được chuyển thành PENDING (để nộp bài duyệt)
        if (isInstructor) {
            if (!"PENDING".equals(newStatus)) {
                throw new AppException(ErrorCode.LESSON_STATUS_FORBIDDEN);
            }
            // Chỉ được nộp bài học của chính mình
            if (lesson.getCreatedBy() == null || !lesson.getCreatedBy().getEmail().equals(userEmail)) {
                throw new AppException(ErrorCode.UNAUTHORIZED_LESSON_ACCESS);
            }
        }

        // ADMIN mới được APPROVED hoặc REJECTED
        if (!isAdmin && !isInstructor) {
            throw new AppException(ErrorCode.LESSON_STATUS_FORBIDDEN);
        }

        // Cập nhật status bài học
        lesson.setStatus(newStatus);
        lesson.setUpdatedAt(LocalDateTime.now());
        lessonRepository.save(lesson);

        // Ghi content_reviews nếu là ADMIN duyệt/từ chối
        if (isAdmin && ("APPROVED".equals(newStatus) || "REJECTED".equals(newStatus))) {
            ContentReview review = ContentReview.builder()
                    .lesson(lesson)
                    .reviewer(actor)
                    .status(newStatus)
                    .comment(request.getComment())
                    .reviewedAt(LocalDateTime.now())
                    .build();
            contentReviewRepository.save(review);
        }

        return LessonStatusResponse.builder()
                .id(lesson.getId())
                .status(lesson.getStatus())
                .build();
    }

    // =========================================================
    // Helpers
    // =========================================================

    /** Kiểm tra quyền: ADMIN toàn quyền, INSTRUCTOR chỉ được sửa bài của mình */
    private void checkLessonPermission(Lesson lesson, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if ("ADMIN".equals(user.getRole().getName())) return;

        if (lesson.getCreatedBy() == null || !lesson.getCreatedBy().getEmail().equals(userEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_LESSON_ACCESS);
        }
    }

    private LessonResponse mapToResponse(Lesson lesson) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .lessonCode(lesson.getLessonCode())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .status(lesson.getStatus())
                .orderIndex(lesson.getOrderIndex())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                // v2.0: skillLevel thay vì difficulty string
                .skillLevel(lesson.getSkillLevel() != null ? LessonResponse.SkillLevelInfo.builder()
                        .id(lesson.getSkillLevel().getId())
                        .levelName(lesson.getSkillLevel().getLevelName())
                        .build() : null)
                .instrument(lesson.getInstrument() != null ? LessonResponse.InstrumentInfo.builder()
                        .id(lesson.getInstrument().getId())
                        .instrumentCode(lesson.getInstrument().getInstrumentCode())
                        .name(lesson.getInstrument().getName())
                        .iconUrl(lesson.getInstrument().getIconUrl())
                        .build() : null)
                .createdBy(lesson.getCreatedBy() != null ? LessonResponse.CreatorInfo.builder()
                        .id(lesson.getCreatedBy().getId())

                        .fullName(lesson.getCreatedBy().getFullName())
                        .role(lesson.getCreatedBy().getRole().getName())
                        .build() : null)
                .techniques(lesson.getTechniques() != null ? lesson.getTechniques().stream()
                        .map(t -> LessonResponse.TechniqueInfo.builder()
                                .id(t.getId())
                                .name(t.getName())
                                .guideUrl(t.getGuideUrl())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .lessonAssets(lesson.getMediaAssets() != null ? lesson.getMediaAssets().stream()
                        .map(a -> LessonResponse.AssetInfo.builder()
                                .id(a.getId())
                                .assetType(a.getAssetType())
                                .assetUrl(a.getAssetUrl())
                                .tempoBpm(a.getTempoBpm())
                                .durationSec(a.getDurationSec())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .exercises(lesson.getExercises() != null ? lesson.getExercises().stream()
                        .map(e -> LessonResponse.ExerciseInfo.builder()
                                .id(e.getId())
                                .title(e.getTitle())
                                .description(e.getDescription())
                                .passThreshold(e.getPassThreshold())
                                .orderIndex(e.getOrderIndex())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
}
