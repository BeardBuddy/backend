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

    public static ReviewDto from(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getAppointment().getId(),
                review.getCustomer().getId(),
                review.getRating(),
                review.getComment(),
                review.getDate()
        );
    }
}
