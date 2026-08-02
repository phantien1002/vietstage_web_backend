package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.AppConfigResponse;
import com.example.vietstage_web_be.entity.AppConfig;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.AppConfigRepository;
import com.example.vietstage_web_be.service.IAppConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppConfigServiceImpl implements IAppConfigService {

    private final AppConfigRepository appConfigRepository;

    @Override
    public List<AppConfigResponse> getAllConfigs(String group) {
        List<AppConfig> configs;
        if (group != null && !group.isEmpty()) {
            configs = appConfigRepository.findByConfigGroup(group);
        } else {
            configs = appConfigRepository.findAll();
        }

        return configs.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public AppConfigResponse updateConfig(String key, String value, User updatedBy) {
        AppConfig config = appConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        config.setConfigValue(value);
        config.setUpdatedBy(updatedBy);
        config.setUpdatedAt(LocalDateTime.now());
        appConfigRepository.save(config);

        return mapToResponse(config);
    }

    @Override
    public Map<String, String> getPublicConfigs(String group) {
        List<AppConfig> configs;
        if (group != null && !group.isEmpty()) {
            configs = appConfigRepository.findByConfigGroup(group);
        } else {
            configs = appConfigRepository.findAll();
        }

        return configs.stream()
                .collect(Collectors.toMap(AppConfig::getConfigKey, AppConfig::getConfigValue));
    }

    private AppConfigResponse mapToResponse(AppConfig config) {
        return AppConfigResponse.builder()
                .key(config.getConfigKey())
                .value(config.getConfigValue())
                .description(config.getDescription())
                .configGroup(config.getConfigGroup())
                .updatedBy(config.getUpdatedBy() != null ? config.getUpdatedBy().getFullName() : null)
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}