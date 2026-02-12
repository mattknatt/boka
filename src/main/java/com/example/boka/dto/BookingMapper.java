package com.example.boka.dto;

import com.example.boka.entity.Booking;

public final class BookingMapper {

    private BookingMapper() {}

    public static BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getFirstName(),
                booking.getUser().getLastName(),
                booking.getGymClass().getId(),
                booking.getGymClass().getClassType().getName(),
                booking.getGymClass().getStartTime(),
                booking.getStatus(),
                booking.getBookedAt(),
                booking.getCancelledAt()
        );
    }
}