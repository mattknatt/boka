package com.example.boka.gymclass.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gym_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GymClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_type_id", nullable = false)
    private ClassType classType;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId; // Referencing User by ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false, insertable = false, updatable = false)
    private GymInfo gym; // Join with local cache for read access

    @Column(name = "gym_id", nullable = false)
    private Long gymId; // Referencing Gym by ID

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassStatus status = ClassStatus.SCHEDULED;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // We removed the list of bookings to keep it decoupled.
    // Booking count will be handled via a port or direct query in the booking module.

    // For now, I'll keep a temporary simple available spots calculation if needed,
    // but in a real hexagonal design, GymClass wouldn't know about bookings.
    // Let's assume for this search view we might need it, or we fetch it differently.

    @Transient
    private Integer availableSpots;
}
