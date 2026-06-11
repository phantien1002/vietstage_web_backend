package com.example.vietstage_web_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LearnerCosmeticsId implements Serializable {

    @Column(name = "learner_id")
    private Long learnerId;

    @Column(name = "cosmetic_item_id")
    private Long cosmeticItemId;
}
