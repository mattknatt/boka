package com.example.boka.booking;

import java.util.Map;
import java.util.Set;

public interface BookingProviderPort {
    Map<Long, Integer> getBookingCounts(Set<Long> gymClassIds);
    Set<Long> getBookedClassIds(Long userId, Set<Long> gymClassIds);
    void cancelBookingsForClass(Long classId);
}
