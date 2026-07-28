package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.ReviewItemResponse;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.MediaAsset;
import com.example.vietstage_web_be.entity.ContentReview;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.ContentReviewRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.service.IAdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements IAdminReviewService {

    private final LessonRepository lessonRepository;
    private final ContentReviewRepository contentReviewRepository;
    private final UserRepository userRepository;

    @Override
    public List<ReviewItemResponse> getAllReviews() {
        List<Lesson> lessons = lessonRepository.findAll();
        return lessons.stream()
                .filter(lesson -> !"DRAFT".equalsIgnoreCase(lesson.getStatus()))
                .map(lesson -> {
            String sheetUrl = "";
            String audioUrl = "";
            String duration = "00:00";
            if (lesson.getMediaAssets() != null) {
                for (MediaAsset asset : lesson.getMediaAssets()) {
                    if ("SHEET_MUSIC".equals(asset.getAssetType()) || "DOCUMENT".equals(asset.getAssetType())) {
                        sheetUrl = asset.getAssetUrl();
                    }
                    if ("AUDIO".equals(asset.getAssetType()) || "VIDEO".equals(asset.getAssetType())) {
                        audioUrl = asset.getAssetUrl();
                        if (asset.getDurationSec() != null) {
                            int totalSecs = asset.getDurationSec().intValue();
                            int min = totalSecs / 60;
                            int sec = totalSecs % 60;
                            duration = String.format("%02d:%02d", min, sec);
                        }
                    }
                }
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dateStr = lesson.getCreatedAt() != null ? lesson.getCreatedAt().format(formatter) : "";

            String instructorName = lesson.getCreatedBy() != null ? lesson.getCreatedBy().getFullName() : "Unknown";
            String inst = lesson.getInstrument() != null ? lesson.getInstrument().getName() : "Unknown";

            ContentReview latestReview = contentReviewRepository
                    .findTopByLessonIdOrderByReviewedAtDesc(lesson.getId())
                    .orElse(null);

            return ReviewItemResponse.builder()
                    .id(lesson.getId())
                    .title(lesson.getTitle())
                    .instrument(inst)
                    .instructor(instructorName)
                    .date(dateStr)
                    .sheetMusicUrl(sheetUrl)
                    .audioUrl(audioUrl)
                    .duration(duration)
                    .description(lesson.getDescription())
                    .status(lesson.getStatus() != null ? lesson.getStatus().toLowerCase() : "pending")
                    .feedback(latestReview != null ? latestReview.getComment() : null)
                    .approvedBy(latestReview != null && latestReview.getReviewer() != null
                            ? latestReview.getReviewer().getFullName() : null)
                    .approvedAt(latestReview != null && latestReview.getReviewedAt() != null
                            ? latestReview.getReviewedAt().format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveReview(Long id, Long adminId) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND, "Bài học không tồn tại"));

        lesson.setStatus("APPROVED");
        lessonRepository.save(lesson);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Admin không tồn tại"));
        
        ContentReview review = ContentReview.builder()
                .lesson(lesson)
                .reviewer(admin)
                .status("APPROVED")
                .comment("Bài giảng đã được duyệt")
                .reviewedAt(LocalDateTime.now())
                .build();
        contentReviewRepository.save(review);
    }

    @Override
    @Transactional
    public void resetReview(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        lesson.setStatus("PENDING");
        lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public void rejectReview(Long id, String feedback, Long adminId) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND, "Bài học không tồn tại"));

        lesson.setStatus("REJECTED");
        lessonRepository.save(lesson);
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Admin không tồn tại"));

        ContentReview review = ContentReview.builder()
                .lesson(lesson)
                .reviewer(admin)
                .status("REJECTED")
                .comment(feedback)
                .reviewedAt(LocalDateTime.now())
                .build();
        contentReviewRepository.save(review);
    }
}
