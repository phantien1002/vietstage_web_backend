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

    @PostMapping("/users/me/cosmetics/{cosmeticId}/purchase")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<com.example.vietstage_web_be.dto.response.PurchaseCosmeticResponse>> purchaseCosmetic(
            @AuthenticationPrincipal(expression = "user") User learner,
            @PathVariable Long cosmeticId,
            @Valid @RequestBody com.example.vietstage_web_be.dto.request.PurchaseCosmeticRequest request) {
        com.example.vietstage_web_be.dto.response.PurchaseCosmeticResponse response = cosmeticsService.purchaseCosmetic(learner, cosmeticId, request);
        return ResponseEntity.ok(BaseResponse.success(response, "Mở khóa vật phẩm thành công"));
    }

    @GetMapping("/users/me/cosmetics/layout")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<com.example.vietstage_web_be.dto.request.CosmeticLayoutRequest>> getCosmeticLayout(
            @AuthenticationPrincipal(expression = "user") User learner) {
        com.example.vietstage_web_be.dto.request.CosmeticLayoutRequest response = cosmeticsService.getCosmeticLayout(learner);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping("/users/me/cosmetics/layout")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<com.example.vietstage_web_be.dto.request.CosmeticLayoutRequest>> saveCosmeticLayout(
            @AuthenticationPrincipal(expression = "user") User learner,
            @Valid @RequestBody com.example.vietstage_web_be.dto.request.CosmeticLayoutRequest request) {
        com.example.vietstage_web_be.dto.request.CosmeticLayoutRequest response = cosmeticsService.saveCosmeticLayout(learner, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/admin/cosmetics")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<List<CosmeticItemResponse>>> getAllCosmeticsForAdmin(
            @RequestParam(required = false) String item_type,
            @RequestParam(required = false) String status) {
        List<CosmeticItemResponse> response = cosmeticsService.getAllCosmeticItemsForAdmin(item_type, status);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/admin/cosmetics")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<CosmeticItemResponse>> createCosmetic(
            @Valid @RequestBody com.example.vietstage_web_be.dto.request.CreateCosmeticRequest request) {
        CosmeticItemResponse response = cosmeticsService.createCosmeticItem(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping("/admin/cosmetics/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<CosmeticItemResponse>> updateCosmetic(
            @PathVariable Long id,
            @Valid @RequestBody com.example.vietstage_web_be.dto.request.UpdateCosmeticRequest request) {
        CosmeticItemResponse response = cosmeticsService.updateCosmeticItem(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/admin/cosmetics/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteCosmetic(@PathVariable Long id) {
        cosmeticsService.deleteCosmeticItem(id);
        return ResponseEntity.ok(BaseResponse.success("Deleted successfully"));
    }
}