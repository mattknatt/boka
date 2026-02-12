package com.example.boka.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

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

    // Relationship: GymClass can have many bookings
    @OneToMany(mappedBy = "gymClass", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    // Helper method to get available spots
    public int getAvailableSpots() {
        long confirmedBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .count();
        return capacity - (int) confirmedBookings;
    }

    // Helper method to check if class is full
    public boolean isFull() {
        return getAvailableSpots() <= 0;
    }
}