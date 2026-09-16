package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "runtime_profile")
public class RuntimeProfile {

    @Id
    @Column(name = "profile_key", nullable = false)
    private String profileKey;

    @Column(name = "config", columnDefinition = "jsonb", nullable = false)
    private String config;
}
