package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.TechniqueRequest;
import com.example.vietstage_web_be.dto.request.UpdateTechniqueRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.TechniqueResponse;
import com.example.vietstage_web_be.service.ITechniqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/techniques")
@RequiredArgsConstructor
@Tag(name = "Techniques")
public class TechniqueController {

    private final ITechniqueService techniqueService;

    /**
     * GET /api/techniques?instrument_id={id}
     * PUBLIC — trả về danh sách kỹ thuật, có thể lọc theo nhạc cụ.
     */
    @GetMapping
    @Operation(summary = "Danh sách kỹ thuật (PUBLIC)")
    public ResponseEntity<ApiResponse<List<TechniqueResponse>>> getTechniques(
            @RequestParam(value = "instrument_id", required = false) Long instrumentId) {

        List<TechniqueResponse> data = (instrumentId != null)
                ? techniqueService.getTechniquesByInstrumentId(instrumentId)
                : techniqueService.getAllTechniques();

        return ResponseEntity.ok(ApiResponse.<List<TechniqueResponse>>builder()
                .message("Get techniques successfully")
                .data(data)
                .build());
    }

    /**
     * POST /api/techniques
     * INSTRUCTOR, ADMIN — tạo kỹ thuật mới, trả về 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Tạo kỹ thuật mới (INSTRUCTOR, ADMIN)")
    public ResponseEntity<ApiResponse<TechniqueResponse>> createTechnique(
            @RequestBody @Valid TechniqueRequest request) {

        TechniqueResponse data = techniqueService.createTechnique(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TechniqueResponse>builder()
                        .message("Technique created successfully")
                        .data(data)
                        .build());
    }

    /**
     * PUT /api/techniques/{id}
     * INSTRUCTOR, ADMIN — cập nhật name/description/guide_url.
     * instrument_id KHÔNG được thay đổi sau khi tạo.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Cập nhật kỹ thuật (INSTRUCTOR, ADMIN)")
    public ResponseEntity<ApiResponse<TechniqueResponse>> updateTechnique(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTechniqueRequest request) {

        TechniqueResponse data = techniqueService.updateTechnique(id, request);
        return ResponseEntity.ok(ApiResponse.<TechniqueResponse>builder()
                .message("Technique updated successfully")
                .data(data)
                .build());
    }

    /**
     * DELETE /api/techniques/{id}
     * ADMIN only — xóa kỹ thuật, trả về 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa kỹ thuật (ADMIN only)")
    public ResponseEntity<Void> deleteTechnique(@PathVariable Long id) {
        techniqueService.deleteTechnique(id);
        return ResponseEntity.noContent().build();
    }
}
