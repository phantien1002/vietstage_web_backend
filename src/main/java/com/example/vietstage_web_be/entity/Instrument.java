package com.example.vietstage_web_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL)
    private List<Technique> techniques;

    @OneToMany(mappedBy = "instrument")
    private List<Lesson> lessons;
}
