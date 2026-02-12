package com.example.boka.repository;

import com.example.boka.entity.User;
import com.example.boka.entity.UserRole;

import java.util.Set;

public interface UserRepository {
    User save(User user);
    boolean delete(User user);
    User findById(Long id);
    User findByRole(UserRole role);
    User findByEmail(String email);
    User findByFirstName(String firstName);
    User findByLastName(String lastName);
    User findByPhoneNumber(String phoneNumber);
    Set<User> findAll();
    Set<User> findActiveUsers();
}
