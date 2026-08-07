package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.AssetResponse;
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
import java.util.ArrayList;
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
        List<Lesson> pendingLessons = lessonRepository.findByStatusIgnoreCase("PENDING");
        return pendingLessons.stream().map(lesson -> {
            List<AssetResponse> assets = new ArrayList<>();
            if (lesson.getMediaAssets() != null) {
                for (MediaAsset asset : lesson.getMediaAssets()) {
                    String mimeType = "application/octet-stream";
                    if (asset.getAssetType() != null) {
                        if (asset.getAssetType().contains("AUDIO")) {
                            mimeType = "audio/mpeg";
                        } else if (asset.getAssetType().contains("VIDEO")) {
                            mimeType = "video/mp4";
                        } else if (asset.getAssetType().contains("IMAGE") || asset.getAssetType().contains("SHEET")) {
                            mimeType = "image/png";
                        } else if (asset.getAssetType().contains("DOCUMENT")) {
                            mimeType = "application/pdf";
                        }
                    }
                    
                    assets.add(AssetResponse.builder()
                            .id(asset.getId())
                            .assetType(asset.getAssetType())
                            .title(asset.getTitle())
                            .assetUrl(asset.getAssetUrl())
                            .mimeType(mimeType)
                            .durationSec(asset.getDurationSec())
                            .build());
                }
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dateStr = lesson.getCreatedAt() != null ? lesson.getCreatedAt().format(formatter) : "";

            String instructorName = lesson.getCreatedBy() != null ? lesson.getCreatedBy().getFullName() : "Unknown";
            String inst = lesson.getInstrument() != null ? lesson.getInstrument().getName() : "Unknown";

            return ReviewItemResponse.builder()
                    .id(lesson.getId())
                    .title(lesson.getTitle())
                    .instrument(inst)
                    .instructor(instructorName)
                    .date(dateStr)
                    .assets(assets)
                    .technicalNotes(lesson.getDescription()) // Mapping description to technical notes for now
                    .description(lesson.getDescription())
                    .status(lesson.getStatus() != null ? lesson.getStatus().toLowerCase() : "pending")
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

