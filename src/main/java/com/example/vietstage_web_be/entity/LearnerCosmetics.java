package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "learner_cosmetics")
public class LearnerCosmetics {

    @EmbeddedId
    private LearnerCosmeticsId id = new LearnerCosmeticsId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("learnerId")
    @JoinColumn(name = "learner_id")
    private Users learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cosmeticItemId")
    @JoinColumn(name = "cosmetic_item_id")
    private CosmeticItems cosmeticItem;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "is_equipped")
    private Boolean isEquipped;
}
