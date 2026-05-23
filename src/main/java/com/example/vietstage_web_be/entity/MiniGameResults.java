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
@Table(name = "mini_game_results")
public class MiniGameResults {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Column(name = "mini_game_id", nullable = false)
    private Long miniGameId;

    @Column(name = "score")
    private Long score;

    @Column(name = "stars_earned")
    private Long starsEarned;

    @Column(name = "played_at")
    private Date playedAt;

}
