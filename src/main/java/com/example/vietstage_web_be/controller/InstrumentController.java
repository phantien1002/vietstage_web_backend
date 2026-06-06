package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.InstrumentRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.InstrumentResponse;
import com.example.vietstage_web_be.dto.response.TechniqueResponse;
import com.example.vietstage_web_be.service.IInstrumentService;
import com.example.vietstage_web_be.service.ITechniqueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
@Tag(name = "Instruments")
public class InstrumentController {

    private final IInstrumentService instrumentService;
    private final ITechniqueService techniqueService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InstrumentResponse>>> getAllInstruments() {
        List<InstrumentResponse> data = instrumentService.getAllInstruments();
        ApiResponse<List<InstrumentResponse>> response = ApiResponse.<List<InstrumentResponse>>builder()
                .message("Get all instruments successfully")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InstrumentResponse>> getInstrumentById(@PathVariable Long id) {
        InstrumentResponse data = instrumentService.getInstrumentById(id);
        ApiResponse<InstrumentResponse> response = ApiResponse.<InstrumentResponse>builder()
                .message("Get instrument successfully")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/techniques")
    public ResponseEntity<ApiResponse<List<TechniqueResponse>>> getTechniquesByInstrument(@PathVariable Long id) {
        List<TechniqueResponse> data = techniqueService.getTechniquesByInstrumentId(id);
        ApiResponse<List<TechniqueResponse>> response = ApiResponse.<List<TechniqueResponse>>builder()
                .message("Get techniques by instrument successfully")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InstrumentResponse>> createInstrument(
            @RequestBody @Valid InstrumentRequest request) {
        InstrumentResponse data = instrumentService.createInstrument(request);
        ApiResponse<InstrumentResponse> response = ApiResponse.<InstrumentResponse>builder()
                .message("Instrument created successfully")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InstrumentResponse>> updateInstrument(
            @PathVariable Long id,
            @RequestBody @Valid InstrumentRequest request) {
        InstrumentResponse data = instrumentService.updateInstrument(id, request);
        ApiResponse<InstrumentResponse> response = ApiResponse.<InstrumentResponse>builder()
                .message("Instrument updated successfully")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInstrument(@PathVariable Long id) {
        instrumentService.deleteInstrument(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Instrument deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
