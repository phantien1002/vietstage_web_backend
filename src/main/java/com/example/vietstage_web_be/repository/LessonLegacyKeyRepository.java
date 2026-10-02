package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LessonLegacyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonLegacyKeyRepository extends JpaRepository<LessonLegacyKey, LessonLegacyKey.LessonLegacyKeyId> {
    Optional<LessonLegacyKey> findByLessonCodeAndActivityKind(String lessonCode, String activityKind);
}
