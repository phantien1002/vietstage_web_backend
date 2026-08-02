package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.LessonAssetRequest;
import com.example.vietstage_web_be.dto.response.LessonAssetResponse;
import com.example.vietstage_web_be.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ILessonAssetService {
    List<LessonAssetResponse> getLessonAssets(Long lessonId, String type);
    LessonAssetResponse uploadAsset(User instructor, Long lessonId, MultipartFile file, String type, Integer tempoBpm, Integer durationSec);
    LessonAssetResponse updateAssetMetadata(User instructor, Long lessonId, Long assetId, LessonAssetRequest request);
    void deleteAsset(User user, Long lessonId, Long assetId);
}