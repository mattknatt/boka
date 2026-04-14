package com.example.boka;

import com.example.boka.gym.application.GymService;
import com.example.boka.gym.domain.Gym;
import com.example.boka.gymclass.domain.GymInfo;
import com.example.boka.gymclass.infrastructure.GymInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
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

        // Assert: Wait for the asynchronous AFTER_COMMIT event to update the cache
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            Optional<GymInfo> cachedInfo = gymInfoRepository.findById(savedGym.getId());
            return cachedInfo.isPresent();
        });

        GymInfo cachedInfo = gymInfoRepository.findById(savedGym.getId()).get();
        assertEquals("Integration Test Gym", cachedInfo.getName());
        assertEquals("123 Test St", cachedInfo.getAddress());
    }
}
