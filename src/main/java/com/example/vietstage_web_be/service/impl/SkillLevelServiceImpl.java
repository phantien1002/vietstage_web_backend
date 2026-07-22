package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.SkillLevelRequest;
import com.example.vietstage_web_be.dto.response.SkillLevelResponse;
import com.example.vietstage_web_be.entity.SkillLevel;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.SkillLevelRepository;
import com.example.vietstage_web_be.service.ISkillLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillLevelServiceImpl implements ISkillLevelService {

    private final SkillLevelRepository skillLevelRepository;

    @Override
    @Transactional
    public SkillLevelResponse createSkillLevel(SkillLevelRequest request) {
        if (skillLevelRepository.existsByLevelCodeIgnoreCase(request.getLevelCode())) {
            throw new AppException(ErrorCode.SKILL_LEVEL_CODE_ALREADY_EXIST);
        }
        if (skillLevelRepository.existsByOrderIndex(request.getOrderIndex())) {
            throw new AppException(ErrorCode.SKILL_LEVEL_ORDER_ALREADY_EXIST);
        }

        SkillLevel skillLevel = SkillLevel.builder()
                .levelCode(request.getLevelCode().toUpperCase())
                .levelName(request.getLevelName())
                .orderIndex(request.getOrderIndex())
                .build();

        SkillLevel saved = skillLevelRepository.save(skillLevel);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillLevelResponse> getAllSkillLevels() {
        return skillLevelRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SkillLevelResponse getSkillLevelById(Long id) {
        SkillLevel skillLevel = skillLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SKILL_LEVEL_NOT_FOUND));
        return mapToResponse(skillLevel);
    }

    @Override
    @Transactional
    public SkillLevelResponse updateSkillLevel(Long id, SkillLevelRequest request) {
        SkillLevel skillLevel = skillLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SKILL_LEVEL_NOT_FOUND));

        if (!skillLevel.getLevelCode().equalsIgnoreCase(request.getLevelCode()) &&
                skillLevelRepository.existsByLevelCodeIgnoreCase(request.getLevelCode())) {
            throw new AppException(ErrorCode.SKILL_LEVEL_CODE_ALREADY_EXIST);
        }

        if (!skillLevel.getOrderIndex().equals(request.getOrderIndex()) &&
                skillLevelRepository.existsByOrderIndex(request.getOrderIndex())) {
            throw new AppException(ErrorCode.SKILL_LEVEL_ORDER_ALREADY_EXIST);
        }

        skillLevel.setLevelCode(request.getLevelCode().toUpperCase());
        skillLevel.setLevelName(request.getLevelName());
        skillLevel.setOrderIndex(request.getOrderIndex());

        SkillLevel updated = skillLevelRepository.save(skillLevel);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSkillLevel(Long id) {
        SkillLevel skillLevel = skillLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SKILL_LEVEL_NOT_FOUND));
        skillLevelRepository.delete(skillLevel);
    }

    private SkillLevelResponse mapToResponse(SkillLevel skillLevel) {
        return SkillLevelResponse.builder()
                .id(skillLevel.getId())
                .levelCode(skillLevel.getLevelCode())
                .levelName(skillLevel.getLevelName())
                .orderIndex(skillLevel.getOrderIndex())
                .build();
    }
}
