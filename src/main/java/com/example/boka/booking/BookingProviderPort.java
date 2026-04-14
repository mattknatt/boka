package com.example.boka.booking;

import java.util.Map;
import java.util.Set;

public interface BookingProviderPort {
    /**
     * Returns a map of GymClassId to the number of confirmed bookings.
     */
    Map<Long, Integer> getBookingCounts(Set<Long> gymClassIds);

    /**
     * Returns a set of GymClassIds that the given user has a confirmed booking for.
     */
    Set<Long> getBookedClassIds(Long userId, Set<Long> gymClassIds);
}
