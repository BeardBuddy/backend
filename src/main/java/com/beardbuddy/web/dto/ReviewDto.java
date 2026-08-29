package com.beardbuddy.web.dto;

import com.beardbuddy.domain.Review;

public record ReviewDto(
        String id,
        String appointmentId,
        String customerId,
        Integer rating,
        String comment,
        String date
) {

    public static ReviewDto from(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getAppointmentId(),
                r.getCustomerId(),
                r.getRating(),
                r.getComment(),
                r.getDate()
        );
    }
}
