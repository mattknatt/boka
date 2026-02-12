package com.example.boka.repository;

import com.example.boka.entity.Booking;
import com.example.boka.entity.BookingStatus;
import com.example.boka.entity.GymClass;
import com.example.boka.entity.User;

import java.util.Set;

public interface BookingRepository {
    void save(Booking booking);
    boolean delete(Booking booking);
    Booking findById(Long id);
    Set<Booking> findByUser(User user);
    Set<Booking> findByClass(GymClass gymClass);
    Set<Booking> findByUserAndClass(User user, GymClass gymClass);
    Set<Booking> findAll();
    Set<Booking> findByStatus(BookingStatus status);
}
