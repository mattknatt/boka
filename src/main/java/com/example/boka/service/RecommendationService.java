package com.example.boka.service;

import com.example.boka.dto.GymClassMapper;
import com.example.boka.dto.GymClassResponse;
import com.example.boka.entity.Booking;
import com.example.boka.entity.ClassStatus;
import com.example.boka.entity.ClassType;
import com.example.boka.repository.BookingRepository;
import com.example.boka.repository.ClassTypeRepository;
import com.example.boka.repository.GymClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BookingRepository bookingRepository;
    private final ClassTypeRepository classTypeRepository;
    private final GymClassRepository gymClassRepository;

    /**
     * Recommends classes based on the user's booking history and semantic similarity.
     */
    @Transactional(readOnly = true)
    public List<GymClassResponse> getRecommendationsForUser(Long userId, int limit) {
        // 1. Get recent bookings for the user
        List<Booking> recentBookings = bookingRepository.findByUserId(userId);

        if (recentBookings.isEmpty()) {
            // Fallback: return most popular upcoming classes (omitted for brevity, just return upcoming)
            return gymClassRepository.findByStatusAndStartTimeAfter(ClassStatus.SCHEDULED, LocalDateTime.now())
                    .stream()
                    .limit(limit)
                    .map(GymClassMapper::toResponse)
                    .collect(Collectors.toList());
        }

        // 2. Identify favorite class types
        List<Long> favoriteTypeIds = recentBookings.stream()
                .map(b -> b.getGymClass().getClassType().getId())
                .distinct()
                .collect(Collectors.toList());

        // 3. Find semantically similar class types for each favorite type
        List<Long> similarTypeIds = favoriteTypeIds.stream()
                .flatMap(typeId -> classTypeRepository.findSimilarTo(typeId, 2).stream())
                .map(ClassType::getId)
                .filter(id -> !favoriteTypeIds.contains(id))
                .distinct()
                .collect(Collectors.toList());

        // 4. Combine favorite and similar types, prioritized
        List<Long> targetTypeIds = favoriteTypeIds;
        targetTypeIds.addAll(similarTypeIds);

        // 5. Find upcoming classes for these types
        return gymClassRepository.findByClassTypeIdInAndStatusAndStartTimeAfter(
                        targetTypeIds, ClassStatus.SCHEDULED, LocalDateTime.now())
                .stream()
                .limit(limit)
                .map(GymClassMapper::toResponse)
                .collect(Collectors.toList());
    }
}
