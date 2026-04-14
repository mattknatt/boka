package com.example.boka.gym.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymInfoRepository extends JpaRepository<GymInfo, Long> {
}
