package com.example.boka.booking.application;

import com.example.boka.booking.UserBookingResponse;
import com.example.boka.booking.domain.Booking;
import com.example.boka.booking.domain.BookingStatus;
import com.example.boka.booking.domain.BookingRepository;
import com.example.boka.common.UserNotFoundException;
import com.example.boka.gymclass.GymClassProviderPort;
import com.example.boka.user.UserProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
                .sorted((a, b) -> b.startTime().compareTo(a.startTime())) // Newest first
                .toList();
    }

    @Transactional
    public BookingResponse createBooking(Long gymClassId, String userEmail) {
        UserProviderPort.UserDetails user = userProviderPort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        // Check if already booked
        bookingRepository.findByUserIdAndGymClassIdAndStatus(user.id(), gymClassId, BookingStatus.CONFIRMED)
                .ifPresent(b -> { throw new IllegalStateException("Already booked this class"); });

        Booking booking = new Booking();
        booking.setUserId(user.id());
        booking.setGymClassId(gymClassId);
        booking.setStatus(BookingStatus.CONFIRMED);

        try {
            Booking saved = bookingRepository.save(booking);
            return new BookingResponse(saved.getId(), gymClassId, user.email(), "CONFIRMED");
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Already booked this class");
        }
    }

    @Transactional
    public void cancelBooking(Long gymClassId, String userEmail) {
        UserProviderPort.UserDetails user = userProviderPort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        Booking booking = bookingRepository.findByUserIdAndGymClassIdAndStatus(user.id(), gymClassId, BookingStatus.CONFIRMED)
                .orElseThrow(() -> new IllegalStateException("No confirmed booking found for this class"));

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }
}
