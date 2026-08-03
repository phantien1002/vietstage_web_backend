package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.CosmeticItemResponse;
import com.example.vietstage_web_be.dto.response.EquipCosmeticResponse;
import com.example.vietstage_web_be.dto.response.MyCosmeticsResponse;
import com.example.vietstage_web_be.entity.User;

import java.util.List;

public interface ICosmeticsService {
    List<CosmeticItemResponse> getAllCosmeticItems(String itemType);
    MyCosmeticsResponse getMyCosmetics(User learner);
    EquipCosmeticResponse equipCosmetic(User learner, Long cosmeticId, boolean isEquipped);
    CosmeticItemResponse createCosmetic(com.example.vietstage_web_be.dto.request.CosmeticRequest request);
    CosmeticItemResponse updateCosmetic(Long id, com.example.vietstage_web_be.dto.request.CosmeticRequest request);
    void deleteCosmetic(Long id);
}