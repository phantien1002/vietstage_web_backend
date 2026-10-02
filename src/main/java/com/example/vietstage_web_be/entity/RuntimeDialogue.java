package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "runtime_dialogue")
@IdClass(RuntimeDialogue.RuntimeDialogueId.class)
public class RuntimeDialogue {

    @Id
    @Column(name = "legacy_code", nullable = false)
    private String legacyCode;

    @Id
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "raw_step", columnDefinition = "jsonb", nullable = false)
    private String rawStep;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuntimeDialogueId implements Serializable {
        private String legacyCode;
        private Integer orderIndex;
    }
}
