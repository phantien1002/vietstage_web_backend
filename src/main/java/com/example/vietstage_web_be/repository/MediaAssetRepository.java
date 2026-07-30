package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    List<MediaAsset> findByLessonId(Long lessonId);
    List<MediaAsset> findByLessonIdAndAssetType(Long lessonId, String assetType);
    void deleteByLessonId(Long lessonId);
}
