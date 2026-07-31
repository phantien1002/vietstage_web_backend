package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.AppConfigResponse;
import com.example.vietstage_web_be.entity.User;

import java.util.List;
import java.util.Map;

public interface IAppConfigService {

    List<AppConfigResponse> getAllConfigs(String group);

    AppConfigResponse updateConfig(String key, String value, User updatedBy);

    Map<String, String> getPublicConfigs(String group);
}