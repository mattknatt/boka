package com.example.boka.service;

import com.example.boka.dto.BookingMapper;
import com.example.boka.dto.BookingResponse;
import com.example.boka.entity.Booking;
import com.example.boka.entity.BookingStatus;
import com.example.boka.entity.GymClass;
import com.example.boka.entity.User;
import com.example.boka.exception.ResourceNotFoundException;
import com.example.boka.repository.BookingRepository;
import com.example.boka.repository.GymClassRepository;
import com.example.boka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final GymClassRepository gymClassRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse createBooking(Long gymClassId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        GymClass gymClass = gymClassRepository.findById(gymClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym class not found with id: " + gymClassId));

        // 1. Check if user already has a confirmed booking for this class
        if (bookingRepository.existsByUserIdAndGymClassId(user.getId(), gymClassId)) {
            throw new IllegalStateException("User already has a booking for this class.");
        }

        // 2. Check if the class is full
        if (gymClass.isFull()) {
            throw new IllegalStateException("Gym class is already full.");
        }

        // 3. Create and save the booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setGymClass(gymClass);
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        // Optional: Trigger side effects (email notifications, etc.)

        return BookingMapper.toResponse(savedBooking);
    }
}
