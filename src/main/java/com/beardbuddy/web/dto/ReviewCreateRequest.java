package com.beardbuddy.web.dto;

public record ReviewCreateRequest(
        String id,
        String appointmentId,
        String customerId,
        Integer rating,
        String comment,
        String date
) {
}
