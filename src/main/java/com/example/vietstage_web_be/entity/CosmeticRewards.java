package com.example.vietstage_web_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "cosmetic_rewards")
public class CosmeticRewards {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "reward_name", nullable = false)
    private String rewardName;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "required_stars")
    private Long requiredStars;

    @Column(name = "created_at")
    private Date createdAt;

}
