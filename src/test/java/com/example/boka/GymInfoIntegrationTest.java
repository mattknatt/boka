package com.example.boka;

import com.example.boka.gym.application.GymService;
import com.example.boka.gym.domain.Gym;
import com.example.boka.gymclass.domain.GymInfo;
import com.example.boka.gymclass.infrastructure.GymInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class GymInfoIntegrationTest {

    @Autowired
    private GymService gymService;

    @Autowired
    private GymInfoRepository gymInfoRepository;

    @Test
    void whenGymIsSaved_thenGymInfoCacheIsUpdated() {
        // Arrange
        Gym gym = new Gym();
        gym.setName("Integration Test Gym");
        gym.setAddress("123 Test St");
        gym.setLatitude(57.7);
        gym.setLongitude(11.9);

        // Act
        Gym savedGym = gymService.saveGym(gym);

        // Assert: Check if the event listener in gymclass module updated the cache
        Optional<GymInfo> cachedInfo = gymInfoRepository.findById(savedGym.getId());

        assertTrue(cachedInfo.isPresent(), "GymInfo should be present in cache after event publication");
        assertEquals("Integration Test Gym", cachedInfo.get().getName());
        assertEquals("123 Test St", cachedInfo.get().getAddress());
    }
}
