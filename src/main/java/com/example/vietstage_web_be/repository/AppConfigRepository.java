package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {

    Optional<AppConfig> findByConfigKey(String key);

    List<AppConfig> findByConfigGroup(String group);
}