package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.CosmeticItemResponse;
import com.example.vietstage_web_be.dto.response.EquipCosmeticResponse;
import com.example.vietstage_web_be.dto.response.LearnerCosmeticResponse;
import com.example.vietstage_web_be.dto.response.MyCosmeticsResponse;
import com.example.vietstage_web_be.entity.CosmeticItem;
import com.example.vietstage_web_be.entity.LearnerCosmetic;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.CosmeticItemRepository;
import com.example.vietstage_web_be.repository.LearnerCosmeticRepository;
import com.example.vietstage_web_be.service.ICosmeticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.vietstage_web_be.dto.response.PurchaseCosmeticResponse;
import com.example.vietstage_web_be.entity.AuditLog;
import com.example.vietstage_web_be.entity.LearnerProfile;
import com.example.vietstage_web_be.repository.AuditLogRepository;
import com.example.vietstage_web_be.repository.LearnerProfileRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CosmeticsServiceImpl implements ICosmeticsService {

    private final CosmeticItemRepository cosmeticItemRepository;
    private final LearnerCosmeticRepository learnerCosmeticRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public List<CosmeticItemResponse> getAllCosmeticItems(String itemType) {
        List<CosmeticItem> items;
        if (itemType != null && !itemType.isBlank()) {
            items = cosmeticItemRepository.findByItemTypeAndStatus(itemType, "ACTIVE");
        } else {
            items = cosmeticItemRepository.findByStatus("ACTIVE");
        }
        return items.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<CosmeticItemResponse> getAllCosmeticItemsForAdmin(String itemType, String status) {
        List<CosmeticItem> items;
        if (itemType != null && !itemType.isBlank() && status != null && !status.isBlank()) {
            items = cosmeticItemRepository.findByItemTypeAndStatus(itemType, status);
        } else if (itemType != null && !itemType.isBlank()) {
            items = cosmeticItemRepository.findByItemType(itemType);
        } else if (status != null && !status.isBlank()) {
            items = cosmeticItemRepository.findByStatus(status);
        } else {
            items = cosmeticItemRepository.findAll();
        }
        return items.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public MyCosmeticsResponse getMyCosmetics(User learner) {
        List<CosmeticItem> allActiveItems = cosmeticItemRepository.findByStatus("ACTIVE");
        List<LearnerCosmetic> ownedCosmetics = learnerCosmeticRepository.findByLearnerId(learner.getId());
        LearnerProfile profile = learnerProfileRepository.findById(learner.getId()).orElse(null);

        Set<Long> ownedItemIds = ownedCosmetics.stream()
                .map(lc -> lc.getCosmeticItem().getId())
                .collect(Collectors.toSet());

        List<LearnerCosmeticResponse> owned = ownedCosmetics.stream()
                .map(lc -> LearnerCosmeticResponse.builder()
                        .id(lc.getCosmeticItem().getId())
                        .name(lc.getCosmeticItem().getName())
                        .itemType(lc.getCosmeticItem().getItemType())
                        .unlockType(lc.getCosmeticItem().getUnlockType())
                        .unlockValue(lc.getCosmeticItem().getUnlockValue())
                        .assetUrl(lc.getCosmeticItem().getAssetUrl())
                        .isEquipped(lc.getIsEquipped())
                        .build())
                .collect(Collectors.toList());

        List<CosmeticItemResponse> locked = allActiveItems.stream()
                .filter(item -> !ownedItemIds.contains(item.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return MyCosmeticsResponse.builder()
                .totalStars(profile != null ? profile.getTotalStars() : 0)
                .spendableStars(profile != null ? profile.getSpendableStars() : 0)
                .owned(owned)
                .locked(locked)
                .build();
    }

    @Override
    public EquipCosmeticResponse equipCosmetic(User learner, Long cosmeticId, boolean isEquipped) {
        List<LearnerCosmetic> ownedCosmetics = learnerCosmeticRepository.findByLearnerId(learner.getId());
        
        LearnerCosmetic targetCosmetic = ownedCosmetics.stream()
                .filter(lc -> lc.getCosmeticItem().getId().equals(cosmeticId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.COSMETIC_NOT_OWNED));

        if (!"ACTIVE".equals(targetCosmetic.getCosmeticItem().getStatus())) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Should not equip inactive items
        }

        if (isEquipped) {
            // Auto unequip other items of the same type
            String itemType = targetCosmetic.getCosmeticItem().getItemType();
            for (LearnerCosmetic lc : ownedCosmetics) {
                if (Boolean.TRUE.equals(lc.getIsEquipped()) && lc.getCosmeticItem().getItemType().equals(itemType)) {
                    lc.setIsEquipped(false);
                    learnerCosmeticRepository.save(lc);
                }
            }
        }

        targetCosmetic.setIsEquipped(isEquipped);
        learnerCosmeticRepository.save(targetCosmetic);

        return EquipCosmeticResponse.builder()
                .cosmeticId(cosmeticId)
                .isEquipped(isEquipped)
                .build();
    }

    @Override
    @Transactional
    public PurchaseCosmeticResponse purchaseCosmetic(User learner, Long cosmeticId) {
        CosmeticItem item = cosmeticItemRepository.findById(cosmeticId)
                .orElseThrow(() -> new AppException(ErrorCode.COSMETIC_NOT_FOUND));

        if (!"ACTIVE".equals(item.getStatus())) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Item not available
        }

        boolean alreadyOwned = learnerCosmeticRepository.findByLearnerId(learner.getId()).stream()
                .anyMatch(lc -> lc.getCosmeticItem().getId().equals(cosmeticId));
        
        if (alreadyOwned) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Already owned
        }

        LearnerProfile profile = learnerProfileRepository.findById(learner.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (item.getUnlockValue() > 0) {
            if (profile.getSpendableStars() < item.getUnlockValue()) {
                throw new AppException(ErrorCode.BAD_REQUEST); // Not enough stars
            }
            // Deduct stars
            profile.setSpendableStars(profile.getSpendableStars() - item.getUnlockValue());
            learnerProfileRepository.save(profile);
            
            // Log transaction
            AuditLog auditLog = AuditLog.builder()
                    .user(learner)
                    .actionType("COSMETIC_PURCHASE")
                    .entityType("COSMETIC_ITEM")
                    .entityId(String.valueOf(item.getId()))
                    .description("Purchased cosmetic item: " + item.getName() + " for " + item.getUnlockValue() + " stars")
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        }

        // Add ownership record
        LearnerCosmetic ownership = LearnerCosmetic.builder()
                .learner(learner)
                .cosmeticItem(item)
                .isEquipped(false)
                .build();
        learnerCosmeticRepository.save(ownership);

        return PurchaseCosmeticResponse.builder()
                .success(true)
                .message("Mở khóa vật phẩm thành công")
                .data(PurchaseCosmeticResponse.PurchaseData.builder()
                        .cosmeticId(item.getId())
                        .remainingStars(profile.getSpendableStars())
                        .isEquipped(false)
                        .build())
                .build();
    }

    @Override
    public CosmeticItemResponse createCosmeticItem(com.example.vietstage_web_be.dto.request.CreateCosmeticRequest request) {
        CosmeticItem item = CosmeticItem.builder()
                .name(request.getName())
                .itemType(request.getItemType() != null ? request.getItemType() : "ROOM_DECOR")
                .assetUrl(request.getAssetUrl())
                .unlockType(request.getUnlockType() != null ? request.getUnlockType() : "STARS")
                .unlockValue(request.getUnlockValue() != null ? request.getUnlockValue() : 0)
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();
        cosmeticItemRepository.save(item);
        return mapToResponse(item);
    }

    @Override
    public CosmeticItemResponse updateCosmeticItem(Long id, com.example.vietstage_web_be.dto.request.UpdateCosmeticRequest request) {
        CosmeticItem item = cosmeticItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); // Or create a generic NOT_FOUND
        
        if (request.getName() != null) item.setName(request.getName());
        if (request.getItemType() != null) item.setItemType(request.getItemType());
        if (request.getAssetUrl() != null) item.setAssetUrl(request.getAssetUrl());
        if (request.getUnlockType() != null) item.setUnlockType(request.getUnlockType());
        if (request.getUnlockValue() != null) item.setUnlockValue(request.getUnlockValue());
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        
        cosmeticItemRepository.save(item);
        return mapToResponse(item);
    }

    @Override
    public void deleteCosmeticItem(Long id) {
        CosmeticItem item = cosmeticItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Soft delete
        item.setStatus("INACTIVE");
        cosmeticItemRepository.save(item);
    }

    private CosmeticItemResponse mapToResponse(CosmeticItem item) {
        return CosmeticItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .itemType(item.getItemType())
                .unlockType(item.getUnlockType())
                .unlockValue(item.getUnlockValue())
                .assetUrl(item.getAssetUrl())
                .status(item.getStatus())
                .build();
    }
}
