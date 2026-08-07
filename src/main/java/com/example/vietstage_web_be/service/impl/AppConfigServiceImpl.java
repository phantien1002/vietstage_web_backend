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
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy cấu hình này"));

        validateConfigValue(config, value);

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