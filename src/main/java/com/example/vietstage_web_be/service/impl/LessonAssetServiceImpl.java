package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.LessonAssetRequest;
import com.example.vietstage_web_be.dto.response.LessonAssetResponse;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.MediaAsset;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.MediaAssetRepository;
import com.example.vietstage_web_be.service.ILessonAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonAssetServiceImpl implements ILessonAssetService {

    private final MediaAssetRepository assetRepository;
    private final LessonRepository lessonRepository;
    private final com.example.vietstage_web_be.service.IUploadService uploadService;

    @Override
    public List<LessonAssetResponse> getLessonAssets(Long lessonId, String type) {
        List<MediaAsset> assets;
        if (type != null && !type.isEmpty()) {
            assets = assetRepository.findByLessonIdAndAssetType(lessonId, type);
        } else {
            assets = assetRepository.findByLessonId(lessonId);
        }
        return assets.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public LessonAssetResponse uploadAsset(User instructor, Long lessonId, MultipartFile file, String type, Integer tempoBpm, Integer durationSec) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!lesson.getCreatedBy().getId().equals(instructor.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // Validate File
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không thể xác định loại file");
        }
        
        long maxSize = 10 * 1024 * 1024; // 10MB default
        
        if ("REFERENCE_AUDIO".equals(type)) {
            if (!contentType.equals("audio/mpeg") && !contentType.equals("audio/wav") && !contentType.equals("audio/x-wav")) {
                throw new AppException(ErrorCode.BAD_REQUEST, "File audio chỉ hỗ trợ MP3 hoặc WAV");
            }
            maxSize = 20 * 1024 * 1024; // 20MB for audio
        } else if ("SHEET_MUSIC".equals(type)) {
            if (!contentType.equals("image/png") && !contentType.equals("image/jpeg") && !contentType.equals("image/webp")) {
                throw new AppException(ErrorCode.BAD_REQUEST, "File bản nhạc (sheet) chỉ hỗ trợ PNG, JPEG hoặc WebP");
            }
            maxSize = 5 * 1024 * 1024; // 5MB for images
        } else {
            throw new AppException(ErrorCode.BAD_REQUEST, "Loại tài nguyên không hợp lệ");
        }
        
        if (file.getSize() > maxSize) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Kích thước file vượt quá giới hạn cho phép (" + (maxSize / 1024 / 1024) + "MB)");
        }

        // Real Cloudinary upload
        String fileUrl = uploadService.uploadFile(file);

        MediaAsset asset = MediaAsset.builder()
                .lesson(lesson)
                .assetType(type)
                .assetUrl(fileUrl)
                .title(file.getOriginalFilename())
                .mimeType(contentType)
                .tempoBpm(tempoBpm)
                .durationSec(durationSec != null ? java.math.BigDecimal.valueOf(durationSec) : null)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        
        assetRepository.save(asset);
        return mapToResponse(asset);
    }

    @Override
    @Transactional
    public LessonAssetResponse updateAssetMetadata(User instructor, Long lessonId, Long assetId, LessonAssetRequest request) {
        MediaAsset asset = validateOwnership(instructor, lessonId, assetId);
        
        asset.setTempoBpm(request.getTempoBpm());
        if (request.getDurationSec() != null) {
            asset.setDurationSec(java.math.BigDecimal.valueOf(request.getDurationSec()));
        } else {
            asset.setDurationSec(null);
        }
        
        assetRepository.save(asset);
        return mapToResponse(asset);
    }

    @Override
    @Transactional
    public void deleteAsset(User user, Long lessonId, Long assetId) {
        MediaAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                
        if (!asset.getLesson().getId().equals(lessonId)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Admin or Instructor who created the lesson can delete
        boolean isAdmin = user.getRole() != null && "ADMIN".equals(user.getRole().getName());
        boolean isOwner = asset.getLesson().getCreatedBy().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        assetRepository.delete(asset);
    }

    private MediaAsset validateOwnership(User instructor, Long lessonId, Long assetId) {
        MediaAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!asset.getLesson().getId().equals(lessonId)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (!asset.getLesson().getCreatedBy().getId().equals(instructor.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        
        return asset;
    }

    private LessonAssetResponse mapToResponse(MediaAsset asset) {
        return LessonAssetResponse.builder()
                .id(asset.getId())
                .type(asset.getAssetType())
                .url(asset.getAssetUrl())
                .tempoBpm(asset.getTempoBpm())
                .title(asset.getTitle())
                .mimeType(asset.getMimeType())
                .durationSec(asset.getDurationSec() != null ? asset.getDurationSec().intValue() : null)
                .build();
    }
}
