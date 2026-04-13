package com.example.boka.booking.application;

import com.example.boka.booking.domain.Booking;
import com.example.boka.booking.domain.BookingStatus;
import com.example.boka.booking.infrastructure.BookingRepository;
import com.example.boka.common.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserProviderPort userProviderPort;

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
}
