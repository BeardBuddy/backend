package com.beardbuddy.web.dto;

import com.beardbuddy.domain.User;

public record CustomerDto(
        String id,
        String firstName,
        String lastName,
        String fullName,
        String phone,
        String email,
        Integer loyaltyPoints
) {

    public static CustomerDto from(User user) {
        return new CustomerDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getLoyaltyPoints()
        );
    }
}
