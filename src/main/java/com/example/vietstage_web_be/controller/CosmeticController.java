package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.EquipCosmeticRequest;
import com.example.vietstage_web_be.dto.response.CosmeticItemResponse;
import com.example.vietstage_web_be.dto.response.EquipCosmeticResponse;
import com.example.vietstage_web_be.dto.response.MyCosmeticsResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.ICosmeticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Cosmetics", description = "Các API quản lý vật phẩm trang trí")
public class CosmeticController {

    private final ICosmeticsService cosmeticsService;

    @GetMapping("/cosmetics")
    public ResponseEntity<BaseResponse<List<CosmeticItemResponse>>> getAllCosmetics(
            @RequestParam(required = false) String item_type) {
        List<CosmeticItemResponse> response = cosmeticsService.getAllCosmeticItems(item_type);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/users/me/cosmetics")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<MyCosmeticsResponse>> getMyCosmetics(
            @AuthenticationPrincipal(expression = "user") User learner) {
        MyCosmeticsResponse response = cosmeticsService.getMyCosmetics(learner);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping("/users/me/cosmetics/{cosmeticId}")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<EquipCosmeticResponse>> equipCosmetic(
            @AuthenticationPrincipal(expression = "user") User learner,
            @PathVariable Long cosmeticId,
            @Valid @RequestBody EquipCosmeticRequest request) {
        EquipCosmeticResponse response = cosmeticsService.equipCosmetic(learner, cosmeticId, request.getIsEquipped());
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}