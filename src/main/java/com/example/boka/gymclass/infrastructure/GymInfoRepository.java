package com.example.boka.gymclass.infrastructure;

import com.example.boka.gymclass.domain.GymInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymInfoRepository extends JpaRepository<GymInfo, Long> {
}
