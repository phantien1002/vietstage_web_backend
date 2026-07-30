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

@Service
@RequiredArgsConstructor
public class CosmeticsServiceImpl implements ICosmeticsService {

    private final CosmeticItemRepository cosmeticItemRepository;
    private final LearnerCosmeticRepository learnerCosmeticRepository;

    @Override
    public List<CosmeticItemResponse> getAllCosmeticItems(String itemType) {
        List<CosmeticItem> items;
        if (itemType != null && !itemType.isBlank()) {
            items = cosmeticItemRepository.findByItemType(itemType);
        } else {
            items = cosmeticItemRepository.findAll();
        }
        return items.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public MyCosmeticsResponse getMyCosmetics(User learner) {
        List<CosmeticItem> allItems = cosmeticItemRepository.findAll();
        List<LearnerCosmetic> ownedCosmetics = learnerCosmeticRepository.findByLearnerId(learner.getId());

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

        List<CosmeticItemResponse> locked = allItems.stream()
                .filter(item -> !ownedItemIds.contains(item.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return MyCosmeticsResponse.builder()
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

    private CosmeticItemResponse mapToResponse(CosmeticItem item) {
        return CosmeticItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .itemType(item.getItemType())
                .unlockType(item.getUnlockType())
                .unlockValue(item.getUnlockValue())
                .assetUrl(item.getAssetUrl())
                .build();
    }
}
