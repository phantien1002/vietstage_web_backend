package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.CosmeticItemResponse;
import com.example.vietstage_web_be.dto.response.EquipCosmeticResponse;
import com.example.vietstage_web_be.dto.response.MyCosmeticsResponse;
import com.example.vietstage_web_be.entity.User;

import java.util.List;

public interface ICosmeticsService {
    List<CosmeticItemResponse> getAllCosmeticItems(String itemType);
    List<CosmeticItemResponse> getAllCosmeticItemsForAdmin(String itemType, String status);
    MyCosmeticsResponse getMyCosmetics(User learner);
    EquipCosmeticResponse equipCosmetic(User learner, Long cosmeticId, boolean isEquipped);
    com.example.vietstage_web_be.dto.response.PurchaseCosmeticResponse purchaseCosmetic(User learner, Long cosmeticId, com.example.vietstage_web_be.dto.request.PurchaseCosmeticRequest request);
    com.example.vietstage_web_be.dto.request.CosmeticLayoutRequest getCosmeticLayout(User learner);
    com.example.vietstage_web_be.dto.request.CosmeticLayoutRequest saveCosmeticLayout(User learner, com.example.vietstage_web_be.dto.request.CosmeticLayoutRequest layout);
    CosmeticItemResponse createCosmeticItem(com.example.vietstage_web_be.dto.request.CreateCosmeticRequest request);
    CosmeticItemResponse updateCosmeticItem(Long id, com.example.vietstage_web_be.dto.request.UpdateCosmeticRequest request);
    void deleteCosmeticItem(Long id);
}