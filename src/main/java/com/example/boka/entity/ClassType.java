package com.example.boka.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "class_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    // Vector embedding of the description (1536 dims for OpenAI text-embedding-3-small)
    @Column(name = "description_embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 768)
    private float[] descriptionEmbedding;

    @Column(nullable = false)
    private Integer defaultCapacity;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Boolean isActive = true;

    // Relationship: ClassType can have many classes
    @OneToMany(mappedBy = "classType", cascade = CascadeType.ALL)
    private List<GymClass> gymClasses = new ArrayList<>();
}