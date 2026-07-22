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
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true)
    private String name; // ADMIN | INSTRUCTOR | LEARNER

    @OneToMany(mappedBy = "role")
    private List<User> User;
}
