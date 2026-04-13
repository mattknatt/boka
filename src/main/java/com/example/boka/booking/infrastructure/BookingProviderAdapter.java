package com.example.boka.booking.infrastructure;

import com.example.boka.gymclass.application.BookingProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingProviderAdapter implements BookingProviderPort {

    private final BookingRepository bookingRepository;

    @Override
    public Map<Long, Integer> getBookingCounts(Set<Long> gymClassIds) {
        // Find all confirmed bookings for the given class IDs and group by class ID
        return gymClassIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> bookingRepository.findByGymClassId(id).size() // Simple implementation
                ));
    }
}
