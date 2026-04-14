package com.example.boka.booking;

import java.util.Map;
import java.util.Set;

public interface BookingProviderPort {
    /**
     * Returns a map of GymClassId to the number of confirmed bookings.
     */
    Map<Long, Integer> getBookingCounts(Set<Long> gymClassIds);
}
