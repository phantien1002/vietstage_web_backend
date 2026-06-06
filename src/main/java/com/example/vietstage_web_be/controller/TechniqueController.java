package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.TechniqueRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.TechniqueResponse;
import com.example.vietstage_web_be.service.ITechniqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Techniques", description = "APIs for managing techniques of instruments")
public class TechniqueController {

    private final ITechniqueService techniqueService;

    // --- PUBLIC READ APIS ---

    @GetMapping("/api/instruments/{id}/techniques")
    @Operation(summary = "Get techniques by instrument ID")
    public ResponseEntity<ApiResponse<List<TechniqueResponse>>> getTechniquesByInstrument(@PathVariable Long id) {
        List<TechniqueResponse> data = techniqueService.getTechniquesByInstrumentId(id);
        ApiResponse<List<TechniqueResponse>> response = ApiResponse.<List<TechniqueResponse>>builder()
                .message("Get techniques by instrument successfully")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/techniques/{id}")
    @Operation(summary = "Get technique detail by ID")
    public ResponseEntity<ApiResponse<TechniqueResponse>> getTechniqueById(@PathVariable Long id) {
        TechniqueResponse data = techniqueService.getTechniqueById(id);
        ApiResponse<TechniqueResponse> response = ApiResponse.<TechniqueResponse>builder()
                .message("Get technique successfully")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    // --- ADMIN WRITE APIS (PreAuthorize handled for role security) ---

    @PostMapping("/api/techniques")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new technique (ADMIN only)")
    public ResponseEntity<ApiResponse<TechniqueResponse>> createTechnique(
            @RequestBody @Valid TechniqueRequest request) {
        TechniqueResponse data = techniqueService.createTechnique(request);
        ApiResponse<TechniqueResponse> response = ApiResponse.<TechniqueResponse>builder()
                .message("Technique created successfully")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/techniques/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a technique (ADMIN only)")
    public ResponseEntity<ApiResponse<TechniqueResponse>> updateTechnique(
            @PathVariable Long id,
            @RequestBody @Valid TechniqueRequest request) {
        TechniqueResponse data = techniqueService.updateTechnique(id, request);
        ApiResponse<TechniqueResponse> response = ApiResponse.<TechniqueResponse>builder()
                .message("Technique updated successfully")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/techniques/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a technique (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteTechnique(@PathVariable Long id) {
        techniqueService.deleteTechnique(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Technique deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
