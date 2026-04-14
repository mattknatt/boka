package com.example.boka.booking;

import com.example.boka.booking.domain.BookingStatus;
import com.example.boka.booking.domain.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingProviderAdapter implements BookingProviderPort {

    private final BookingRepository bookingRepository;

    @Override
    public Map<Long, Integer> getBookingCounts(Set<Long> gymClassIds) {
        if (gymClassIds == null || gymClassIds.isEmpty()) {
            return new HashMap<>();
        }

        // Fetch counts for all requested IDs in one query, filtered by CONFIRMED status
        List<Object[]> results = bookingRepository.findCountsByGymClassIdsAndStatus(gymClassIds, BookingStatus.CONFIRMED);

        // Convert results to a Map
        Map<Long, Integer> countsMap = results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Integer) row[1]
                ));

        // Ensure all requested IDs are in the map, defaulting to 0
        return gymClassIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> countsMap.getOrDefault(id, 0)
                ));
    }
}
