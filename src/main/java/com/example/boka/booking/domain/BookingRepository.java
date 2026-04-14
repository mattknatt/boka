package com.example.boka.booking.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByGymClassId(Long gymClassId);
    Optional<Booking> findByUserIdAndGymClassIdAndStatus(Long userId, Long gymClassId, BookingStatus status);

    @Query("SELECT b.gymClassId, COUNT(b) FROM Booking b WHERE b.status = :status AND b.gymClassId IN :gymClassIds GROUP BY b.gymClassId")
    List<Object[]> findCountsByGymClassIdsAndStatus(@Param("gymClassIds") Set<Long> gymClassIds, @Param("status") BookingStatus status);
}
