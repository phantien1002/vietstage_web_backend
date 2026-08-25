package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cosmetic_items")
public class CosmeticItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "item_type", nullable = false)
    @Builder.Default
    private String itemType = "ROOM_DECOR"; // ROOM_DECOR | INSTRUMENT_SKIN | AVATAR_SKIN

    @Column(name = "asset_url")
    private String assetUrl;

    @Column(name = "unlock_type")
    @Builder.Default
    private String unlockType = "STARS"; // ACHIEVEMENT | STARS | POINTS | DEFAULT

    @Column(name = "unlock_value")
    @Builder.Default
    private Integer unlockValue = 0;

    @Column(name = "status")
    @Builder.Default
    private String status = "ACTIVE";
}
