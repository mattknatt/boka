package com.example.boka.gymclass.application;

import com.example.boka.gym.GymUpdatedEvent;
import com.example.boka.gymclass.domain.GymInfo;
import com.example.boka.gymclass.infrastructure.GymInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void handleGymUpdated(GymUpdatedEvent event) {
        log.info("Updating gym info cache for gym ID: {}", event.id());
        GymInfo gymInfo = new GymInfo(event.id(), event.name(), event.address());
        gymInfoRepository.save(gymInfo);
    }
}
