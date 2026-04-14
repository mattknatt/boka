package com.example.boka.gym.application;

import com.example.boka.gym.GymUpdatedEvent;
import com.example.boka.gym.domain.GymInfo;
import com.example.boka.gym.domain.GymInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class GymEventListener {

    private final GymInfoRepository gymInfoRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void handleGymUpdated(GymUpdatedEvent event) {
        if (event.name() == null || event.address() == null) {
            log.error("Skipping gym info cache update for gym ID {} due to missing required fields", event.id());
            return;
        }
        try {
            log.info("Updating gym info cache for gym ID: {}", event.id());
            GymInfo gymInfo = new GymInfo(event.id(), event.name(), event.address());
            gymInfoRepository.save(gymInfo);
        } catch (DataAccessException ex) {
            log.error("Failed to update gym info cache for gym ID {}", event.id(), ex);
        }
    }
}
