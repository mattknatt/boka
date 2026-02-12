package com.example.boka.repository;

import com.example.boka.entity.Booking;
import com.example.boka.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByGymClassId(Long gymClassId);

    Optional<Booking> findByUserIdAndGymClassId(Long userId, Long gymClassId);

    boolean existsByUserIdAndGymClassId(Long userId, Long gymClassId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    long countByGymClassIdAndStatus(Long gymClassId, BookingStatus status);
}