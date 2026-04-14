package com.example.boka.booking.application;

import com.example.boka.booking.UserBookingResponse;
import com.example.boka.booking.domain.Booking;
import com.example.boka.booking.domain.BookingStatus;
import com.example.boka.booking.domain.BookingRepository;
import com.example.boka.gymclass.GymClassProviderPort;
import com.example.boka.user.UserProviderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserProviderPort userProviderPort;

    @Mock
    private GymClassProviderPort gymClassProviderPort;

    @InjectMocks
    private BookingService bookingService;

    private final String testEmail = "test@example.com";
    private final Long testClassId = 100L;
    private final Long testUserId = 1L;

    @Test
    void createBooking_Successful() {
        // Arrange
        UserProviderPort.UserDetails userDetails = new UserProviderPort.UserDetails(testUserId, testEmail);
        when(userProviderPort.findByEmail(testEmail)).thenReturn(Optional.of(userDetails));
        when(bookingRepository.findByUserIdAndGymClassIdAndStatus(testUserId, testClassId, BookingStatus.CONFIRMED))
                .thenReturn(Collections.emptyList());

        Booking savedBooking = new Booking();
        savedBooking.setId(500L);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // Act
        BookingResponse response = bookingService.createBooking(testClassId, testEmail);

        // Assert
        assertNotNull(response);
        assertEquals("CONFIRMED", response.status());
        assertEquals(testEmail, response.userEmail());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_AlreadyBooked_ThrowsException() {
        // Arrange
        UserProviderPort.UserDetails userDetails = new UserProviderPort.UserDetails(testUserId, testEmail);
        when(userProviderPort.findByEmail(testEmail)).thenReturn(Optional.of(userDetails));
        when(bookingRepository.findByUserIdAndGymClassIdAndStatus(testUserId, testClassId, BookingStatus.CONFIRMED))
                .thenReturn(List.of(new Booking()));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(testClassId, testEmail));
        assertEquals("Already booked this class", exception.getMessage());
    }

    @Test
    void cancelBooking_Successful() {
        // Arrange
        UserProviderPort.UserDetails userDetails = new UserProviderPort.UserDetails(testUserId, testEmail);
        when(userProviderPort.findByEmail(testEmail)).thenReturn(Optional.of(userDetails));

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setStatus(BookingStatus.CONFIRMED);

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByUserIdAndGymClassIdAndStatus(testUserId, testClassId, BookingStatus.CONFIRMED))
                .thenReturn(List.of(booking1, booking2));

        // Act
        bookingService.cancelBooking(testClassId, testEmail);

        // Assert
        assertEquals(BookingStatus.CANCELLED, booking1.getStatus());
        assertEquals(BookingStatus.CANCELLED, booking2.getStatus());
        assertNotNull(booking1.getCancelledAt());
        assertNotNull(booking2.getCancelledAt());
        verify(bookingRepository, times(1)).saveAll(anyList());
    }

    @Test
    void cancelBooking_NotFound_ThrowsException() {
        // Arrange
        UserProviderPort.UserDetails userDetails = new UserProviderPort.UserDetails(testUserId, testEmail);
        when(userProviderPort.findByEmail(testEmail)).thenReturn(Optional.of(userDetails));
        when(bookingRepository.findByUserIdAndGymClassIdAndStatus(testUserId, testClassId, BookingStatus.CONFIRMED))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.cancelBooking(testClassId, testEmail));
        assertEquals("No confirmed booking found for this class", exception.getMessage());
    }

    @Test
    void getUserBookings_ReturnsEnrichedBookings() {
        // Arrange
        UserProviderPort.UserDetails userDetails = new UserProviderPort.UserDetails(testUserId, testEmail);
        when(userProviderPort.findByEmail(testEmail)).thenReturn(Optional.of(userDetails));

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setGymClassId(testClassId);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByUserId(testUserId)).thenReturn(List.of(booking));

        LocalDateTime now = LocalDateTime.now();
        GymClassProviderPort.GymClassDetails classDetails = new GymClassProviderPort.GymClassDetails(
                testClassId, "Yoga", now, "Central Gym"
        );
        when(gymClassProviderPort.getGymClassDetails(anyList())).thenReturn(Map.of(testClassId, classDetails));

        // Act
        List<UserBookingResponse> results = bookingService.getUserBookings(testEmail);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Yoga", results.get(0).classTypeName());
        assertEquals("Central Gym", results.get(0).gymName());
        assertEquals(now, results.get(0).startTime());
    }
}
