package com.example.boka.dto;

import com.example.boka.entity.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        if(user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }

    public static User toEntity(UserRegistrationRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        return user;
    }
}