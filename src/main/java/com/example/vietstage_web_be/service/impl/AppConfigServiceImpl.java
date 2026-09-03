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
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> ALLOWED_GROUPS = Arrays.asList("scoring", "difficulty", "feature");
    private static final List<String> ALLOWED_KEYS = Arrays.asList(
            "scoring.star3.threshold", "scoring.star2.threshold", "scoring.star1.threshold",
            "scoring.points.multiplier", "scoring.quiz.points", "scoring.quiz.stars",
            "scoring.minigame.points_per_star", "scoring.minigame.star1_threshold",
            "scoring.minigame.star2_threshold", "scoring.minigame.star3_threshold",
            "feature.leaderboard.enabled", "feature.minigame.enabled",
            "difficulty.rhythm.tolerance", "difficulty.pitch.tolerance"
    );

    @jakarta.annotation.PostConstruct
    public void initDefaultConfigs() {
        seedConfigIfAbsent("scoring.quiz.points", "10", "scoring", "Điểm kinh nghiệm (XP) thưởng cho mỗi câu trắc nghiệm đúng", "NUMBER", 1.0, 100.0, 1.0, "10", true);
        seedConfigIfAbsent("scoring.quiz.stars", "2", "scoring", "Số sao thưởng cho mỗi câu trắc nghiệm đúng", "NUMBER", 1.0, 10.0, 1.0, "2", true);
        String legacyPointsPerStar = appConfigRepository.findByConfigKey("scoring.minigame.multiplier")
                .map(AppConfig::getConfigValue).orElse("5");
        seedConfigIfAbsent("scoring.minigame.points_per_star", legacyPointsPerStar, "scoring", "Điểm kinh nghiệm (XP) thưởng cho mỗi sao Mini Game", "NUMBER", 0.0, 50.0, 1.0, "5", true);
        seedConfigIfAbsent("scoring.minigame.star1_threshold", "50", "scoring", "Tỷ lệ điểm tối thiểu để nhận 1 sao Mini Game", "NUMBER", 0.0, 100.0, 1.0, "50", true);
        seedConfigIfAbsent("scoring.minigame.star2_threshold", "70", "scoring", "Tỷ lệ điểm tối thiểu để nhận 2 sao Mini Game", "NUMBER", 0.0, 100.0, 1.0, "70", true);
        seedConfigIfAbsent("scoring.minigame.star3_threshold", "90", "scoring", "Tỷ lệ điểm tối thiểu để nhận 3 sao Mini Game", "NUMBER", 0.0, 100.0, 1.0, "90", true);
        seedConfigIfAbsent("feature.minigame.enabled", "true", "feature", "Cho phép người học truy cập và nộp Mini Game", "BOOLEAN", null, null, null, "true", true);
    }

    private void seedConfigIfAbsent(String key, String value, String group, String description, String valueType, Double min, Double max, Double step, String defaultValue, Boolean isPublic) {
        if (appConfigRepository.findByConfigKey(key).isEmpty()) {
            AppConfig config = AppConfig.builder()
                    .configKey(key)
                    .configValue(value)
                    .configGroup(group)
                    .description(description)
                    .valueType(valueType)
                    .minValue(min)
                    .maxValue(max)
                    .stepValue(step)
                    .defaultValue(defaultValue)
                    .isPublic(isPublic)
                    .version(1L)
                    .updatedAt(LocalDateTime.now())
                    .build();
            appConfigRepository.save(config);
        }
    }

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
    public AppConfigResponse updateConfig(String key, com.example.vietstage_web_be.dto.request.ConfigUpdateRequest request, User updatedBy) {
        if (!ALLOWED_KEYS.contains(key)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không được phép sửa cấu hình này");
        }

        AppConfig config = appConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy cấu hình này"));
        
        if (request.getVersion() == null || !request.getVersion().equals(config.getVersion())) {
            throw new AppException(ErrorCode.CONFLICT, "Cấu hình đã được cập nhật bởi quản trị viên khác. Vui lòng tải lại dữ liệu.");
        }

        String value = request.getValue();
        validateConfigValue(config, value);
        validateRelatedScoringRules(key, value);

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
                    if (config.getStepValue() != null && config.getStepValue() > 0) {
                        double count = numVal / config.getStepValue();
                        if (Math.abs(count - Math.round(count)) > 0.0001) {
                            throw new AppException(ErrorCode.BAD_REQUEST, "Giá trị phải tuân thủ bước nhảy (step) là " + config.getStepValue());
                        }
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

    private void validateRelatedScoringRules(String key, String proposedValue) {
        if (!Arrays.asList(
                "scoring.minigame.star1_threshold",
                "scoring.minigame.star2_threshold",
                "scoring.minigame.star3_threshold").contains(key)) {
            return;
        }
        double star1 = numericConfig("scoring.minigame.star1_threshold", 50.0, key, proposedValue);
        double star2 = numericConfig("scoring.minigame.star2_threshold", 70.0, key, proposedValue);
        double star3 = numericConfig("scoring.minigame.star3_threshold", 90.0, key, proposedValue);
        if (!(star1 < star2 && star2 < star3)) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Ngưỡng sao Mini Game phải thỏa mãn: 1 sao < 2 sao < 3 sao");
        }
    }

    private double numericConfig(String key, double fallback, String proposedKey, String proposedValue) {
        String value = key.equals(proposedKey)
                ? proposedValue
                : appConfigRepository.findByConfigKey(key).map(AppConfig::getConfigValue).orElse(String.valueOf(fallback));
        return Double.parseDouble(value);
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
                .version(config.getVersion())
                .build();
    }
}
