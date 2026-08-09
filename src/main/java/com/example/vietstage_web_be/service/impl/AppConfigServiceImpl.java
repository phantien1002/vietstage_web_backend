package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.AppConfigResponse;
import com.example.vietstage_web_be.entity.AppConfig;
import com.example.vietstage_web_be.entity.ConfigAuditLog;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.AppConfigRepository;
import com.example.vietstage_web_be.repository.ConfigAuditLogRepository;
import com.example.vietstage_web_be.service.IAppConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppConfigServiceImpl implements IAppConfigService {

    private final AppConfigRepository appConfigRepository;
    private final ConfigAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    private static final List<String> ALLOWED_GROUPS = Arrays.asList("scoring", "difficulty", "feature");
    private static final List<String> ALLOWED_KEYS = Arrays.asList(
            "scoring.star3.threshold", "scoring.star2.threshold", "scoring.star1.threshold",
            "scoring.points.multiplier", "feature.leaderboard.enabled", "feature.minigame.enabled",
            "difficulty.rhythm.tolerance", "difficulty.pitch.tolerance"
    );

    @Override
    public List<AppConfigResponse> getAllConfigs(String group) {
        List<AppConfig> configs;
        if (group != null && !group.isEmpty()) {
            if (!ALLOWED_GROUPS.contains(group)) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Nhóm cấu hình không hợp lệ");
            }
            configs = appConfigRepository.findByConfigGroup(group);
        } else {
            configs = appConfigRepository.findAll().stream()
                    .filter(c -> ALLOWED_GROUPS.contains(c.getConfigGroup()))
                    .collect(Collectors.toList());
        }

        return configs.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public AppConfigResponse updateConfig(String key, String value, User updatedBy) {
        if (!ALLOWED_KEYS.contains(key)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không được phép sửa cấu hình này");
        }

        AppConfig config = appConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy cấu hình này"));

        validateConfigValue(config, value);

        // Audit Log
        ConfigAuditLog auditLog = ConfigAuditLog.builder()
                .configKey(key)
                .oldValue(config.getConfigValue())
                .newValue(value)
                .updatedBy(updatedBy)
                .updatedAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);

        config.setConfigValue(value);
        config.setUpdatedBy(updatedBy);
        config.setUpdatedAt(LocalDateTime.now());
        appConfigRepository.save(config);

        return mapToResponse(config);
    }

    private void validateConfigValue(AppConfig config, String value) {
        if (value == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Giá trị cấu hình không được để trống");
        }

        String type = config.getValueType();
        if (type == null) return;

        switch (type.toUpperCase()) {
            case "BOOLEAN":
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new AppException(ErrorCode.BAD_REQUEST, "Giá trị phải là true hoặc false");
                }
                break;
            case "NUMBER":
                try {
                    double numVal = Double.parseDouble(value);
                    if (config.getMinValue() != null && numVal < config.getMinValue()) {
                        throw new AppException(ErrorCode.BAD_REQUEST, "Giá trị nhỏ nhất cho phép là " + config.getMinValue());
                    }
                    if (config.getMaxValue() != null && numVal > config.getMaxValue()) {
                        throw new AppException(ErrorCode.BAD_REQUEST, "Giá trị lớn nhất cho phép là " + config.getMaxValue());
                    }
                } catch (NumberFormatException e) {
                    throw new AppException(ErrorCode.BAD_REQUEST, "Giá trị phải là một số hợp lệ");
                }
                break;
            case "STRING":
                if (config.getOptions() != null && !config.getOptions().isEmpty()) {
                    List<String> validOptions = List.of(config.getOptions().split(","));
                    if (!validOptions.contains(value)) {
                        throw new AppException(ErrorCode.BAD_REQUEST, "Giá trị phải nằm trong danh sách: " + config.getOptions());
                    }
                }
                break;
            case "JSON":
                try {
                    objectMapper.readTree(value);
                } catch (JsonProcessingException e) {
                    throw new AppException(ErrorCode.BAD_REQUEST, "Giá trị phải là JSON hợp lệ");
                }
                break;
        }
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
                .filter(c -> Boolean.TRUE.equals(c.getIsPublic()))
                .collect(Collectors.toMap(AppConfig::getConfigKey, AppConfig::getConfigValue));
    }

    private AppConfigResponse mapToResponse(AppConfig config) {
        return AppConfigResponse.builder()
                .key(config.getConfigKey())
                .value(config.getConfigValue())
                .description(config.getDescription())
                .valueType(config.getValueType())
                .min(config.getMinValue())
                .max(config.getMaxValue())
                .step(config.getStepValue())
                .options(config.getOptions())
                .defaultValue(config.getDefaultValue())
                .configGroup(config.getConfigGroup())
                .updatedBy(config.getUpdatedBy() != null ? config.getUpdatedBy().getFullName() : null)
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}