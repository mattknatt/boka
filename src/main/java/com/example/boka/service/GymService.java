package com.example.boka.service;

import com.example.boka.dto.GymResponse;
import com.example.boka.repository.GymRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;

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
}
