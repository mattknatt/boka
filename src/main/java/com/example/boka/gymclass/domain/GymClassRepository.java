package com.example.boka.gymclass.domain;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT gc FROM GymClass gc WHERE gc.id = :id")
    Optional<GymClass> findWithLockById(@Param("id") Long id);

    Page<GymClass> findByClassType_IdInAndStatusAndStartTimeAfter(
            List<Long> classTypeIds, ClassStatus status, LocalDateTime time, Pageable pageable
    );

    Page<GymClass> findByStatus(ClassStatus status, Pageable pageable);

    List<GymClass> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
