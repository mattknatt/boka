package com.example.boka.booking.application;

import com.example.boka.booking.domain.Booking;
import com.example.boka.booking.domain.BookingStatus;
import com.example.boka.booking.infrastructure.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserProviderPort userProviderPort;

    @InjectMocks
    private BookingService bookingService;

    private final String testEmail = "test@example.com";
    private final Long testClassId = 100L;
    private final Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        // Reset mocks if needed
    }

    @Test
    void createBooking_Successful() {
        // Arrange
        UserProviderPort.UserDetails userDetails = new UserProviderPort.UserDetails(testUserId, testEmail);
        when(userProviderPort.findByEmail(testEmail)).thenReturn(Optional.of(userDetails));
        when(bookingRepository.findByUserIdAndGymClassIdAndStatus(testUserId, testClassId, BookingStatus.CONFIRMED))
                .thenReturn(Optional.empty());

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
    void createBooking_UserNotFound_ThrowsException() {
        // Arrange
        when(userProviderPort.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(testClassId, testEmail));
    }

    @Test
    void createBooking_AlreadyBooked_ThrowsException() {
        // Arrange
        UserProviderPort.UserDetails userDetails = new UserProviderPort.UserDetails(testUserId, testEmail);
        when(userProviderPort.findByEmail(testEmail)).thenReturn(Optional.of(userDetails));
        when(bookingRepository.findByUserIdAndGymClassIdAndStatus(testUserId, testClassId, BookingStatus.CONFIRMED))
                .thenReturn(Optional.of(new Booking()));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(testClassId, testEmail));
        assertEquals("Already booked this class", exception.getMessage());
    }
}
