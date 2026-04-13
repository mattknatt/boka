package com.example.boka.booking.infrastructure;

import com.example.boka.booking.domain.Booking;
import com.example.boka.booking.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByGymClassId(Long gymClassId);
    Optional<Booking> findByUserIdAndGymClassIdAndStatus(Long userId, Long gymClassId, BookingStatus status);
}
