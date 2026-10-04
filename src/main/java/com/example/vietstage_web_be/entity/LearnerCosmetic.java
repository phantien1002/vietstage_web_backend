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
public class LearnerCosmetic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_user_id")
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cosmetic_item_id")
    private CosmeticItem cosmeticItem;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "is_equipped")
    private Boolean isEquipped;

    @Column(name = "client_request_id")
    private String clientRequestId;
}
