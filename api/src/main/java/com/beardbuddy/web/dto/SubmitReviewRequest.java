package com.beardbuddy.web.dto;

public record SubmitReviewRequest(String customerId, Integer rating, String comment) {
}
