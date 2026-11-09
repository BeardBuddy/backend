package com.beardbuddy.web.dto;

public record RegisterRequest(
        String username,
        String password,
        String firstName,
        String lastName,
        String phone,
        String email
) {
}
