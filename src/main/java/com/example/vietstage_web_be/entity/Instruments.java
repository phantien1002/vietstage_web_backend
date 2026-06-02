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
public class Instruments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL)
    private List<Techniques> techniques;

    @OneToMany(mappedBy = "instrument")
    private List<Lessons> lessons;
}
