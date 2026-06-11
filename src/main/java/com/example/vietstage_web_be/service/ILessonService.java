package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.LessonRequest;
import com.example.vietstage_web_be.dto.request.LessonStatusRequest;
import com.example.vietstage_web_be.dto.request.UpdateLessonRequest;
import com.example.vietstage_web_be.dto.response.LessonResponse;
import com.example.vietstage_web_be.dto.response.LessonStatusResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;

public interface ILessonService {

    /** POST /api/lessons — chỉ INSTRUCTOR. Status mặc định = DRAFT */
    LessonResponse createLesson(LessonRequest request, String userEmail);

    /** GET /api/lessons — PUBLIC. Filter: instrument_id, skill_level_id, status, page, size */
    PageResponse<LessonResponse> getLessons(String search, Long instrumentId, Long skillLevelId,
                                            String status,
                                            int pageNumber, int pageSize);

    /** GET /api/lessons/{id} — PUBLIC. Trả đầy đủ: contents + assets + exercises + techniques + mini_games */
    LessonResponse getLessonById(Long id);

    /** PUT /api/lessons/{id} — INSTRUCTOR, ADMIN. Chỉ cập nhật: title, description, order_index, skill_level_id */
    LessonResponse updateLesson(Long id, UpdateLessonRequest request, String userEmail);

    /** DELETE /api/lessons/{id} — INSTRUCTOR, ADMIN. Trả 204 No Content */
    void deleteLesson(Long id, String userEmail);

    /** PUT /api/lessons/{id}/status — INSTRUCTOR, ADMIN.
     *  INSTRUCTOR chỉ được chuyển sang PENDING.
     *  ADMIN được dùng APPROVED | REJECTED (kèm comment).
     *  Ghi content_reviews + gửi notification cho người tạo. */
    LessonStatusResponse updateLessonStatus(Long id, LessonStatusRequest request, String userEmail);
}
