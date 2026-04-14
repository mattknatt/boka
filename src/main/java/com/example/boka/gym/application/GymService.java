package com.example.boka.gym.application;

import com.example.boka.gym.domain.Gym;
import com.example.boka.gym.GymUpdatedEvent;
import com.example.boka.gym.infrastructure.GymRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Page<GymResponse> getAllGyms(Pageable pageable) {
        return gymRepository.findAll(pageable)
                .map(gym -> new GymResponse(
                        gym.getId(),
                        gym.getName(),
                        gym.getAddress(),
                        gym.getLatitude(),
                        gym.getLongitude()
                ));
    }

    @Transactional
    public Gym saveGym(Gym gym) {
        Gym savedGym = gymRepository.save(gym);
        eventPublisher.publishEvent(new GymUpdatedEvent(
                savedGym.getId(),
                savedGym.getName(),
                savedGym.getAddress()
        ));
        return savedGym;
    }
}
