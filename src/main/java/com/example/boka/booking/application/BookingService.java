package com.example.boka.booking.application;

import com.example.boka.booking.UserBookingResponse;
import com.example.boka.booking.domain.Booking;
import com.example.boka.booking.domain.BookingStatus;
import com.example.boka.booking.domain.BookingRepository;
import com.example.boka.common.ResourceNotFoundException;
import com.example.boka.common.UserNotFoundException;
import com.example.boka.gymclass.GymClassProviderPort;
import com.example.boka.user.UserProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserProviderPort userProviderPort;
    private final GymClassProviderPort gymClassProviderPort;

    @Transactional(readOnly = true)
    public List<UserBookingResponse> getUserBookings(String userEmail) {
        UserProviderPort.UserDetails user = userProviderPort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        List<Booking> bookings = bookingRepository.findByUserId(user.id());

        List<Long> classIds = bookings.stream()
                .map(Booking::getGymClassId)
                .toList();

        Map<Long, GymClassProviderPort.GymClassDetails> classDetails =
                gymClassProviderPort.getGymClassDetails(classIds);

        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED) // Only show active ones for now? Or all? User said "view booked classes"
                .map(b -> {
                    GymClassProviderPort.GymClassDetails details = classDetails.get(b.getGymClassId());
                    return new UserBookingResponse(
                            b.getId(),
                            b.getGymClassId(),
                            details != null ? details.classTypeName() : "Unknown",
                            details != null ? details.startTime() : null,
                            details != null ? details.gymName() : "Local Gym",
                            b.getStatus().name()
                    );
                })
                .sorted(Comparator.comparing(
                        UserBookingResponse::startTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    @Transactional
    public BookingResponse createBooking(Long gymClassId, String userEmail) {
        UserProviderPort.UserDetails user = userProviderPort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        try {
            // Acquire a pessimistic write lock on the gym class row to prevent concurrent overbooking
            int capacity = gymClassProviderPort.lockAndGetCapacity(gymClassId)
                    .orElseThrow(() -> new ResourceNotFoundException("GymClass", "id", gymClassId));

            // Check if already booked
            List<Booking> existingBookings = bookingRepository.findByUserIdAndGymClassIdAndStatus(user.id(), gymClassId, BookingStatus.CONFIRMED);
            if (!existingBookings.isEmpty()) {
                throw new IllegalStateException("Already booked this class");
            }

            // Enforce capacity — count is taken after the lock, so it is accurate
            long confirmedCount = bookingRepository.countByGymClassIdAndStatus(gymClassId, BookingStatus.CONFIRMED);
            if (confirmedCount >= capacity) {
                throw new IllegalStateException("Class is full");
            }

            Booking booking = new Booking();
            booking.setUserId(user.id());
            booking.setGymClassId(gymClassId);
            booking.setStatus(BookingStatus.CONFIRMED);

            Booking saved = bookingRepository.save(booking);
            return new BookingResponse(saved.getId(), gymClassId, user.email(), "CONFIRMED");
        } catch (PessimisticLockingFailureException e) {
            throw new IllegalStateException("Class is temporarily unavailable, please try again");
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Already booked this class");
        }
    }

    @Transactional
    public void cancelBooking(Long gymClassId, String userEmail) {
        UserProviderPort.UserDetails user = userProviderPort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        List<Booking> confirmedBookings = bookingRepository.findByUserIdAndGymClassIdAndStatus(user.id(), gymClassId, BookingStatus.CONFIRMED);

        if (confirmedBookings.isEmpty()) {
            throw new IllegalStateException("No confirmed booking found for this class");
        }

        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : confirmedBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(now);
        }

        bookingRepository.saveAll(confirmedBookings);
    }
}
