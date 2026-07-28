package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.SkillLevelRequest;
import com.example.vietstage_web_be.dto.response.SkillLevelResponse;

import java.util.List;

public interface ISkillLevelService {
    SkillLevelResponse createSkillLevel(SkillLevelRequest request);
    List<SkillLevelResponse> getAllSkillLevels();
    SkillLevelResponse getSkillLevelById(Long id);
    SkillLevelResponse updateSkillLevel(Long id, SkillLevelRequest request);
    void deleteSkillLevel(Long id);
}
