package com.example.boka.gymclass.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "gym_info_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GymInfo {

    @Id
    private Long id; // Same as Gym ID in Gym Module

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;
}
