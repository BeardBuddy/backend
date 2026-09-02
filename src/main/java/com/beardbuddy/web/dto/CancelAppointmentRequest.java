package com.beardbuddy.web.dto;

public record CancelAppointmentRequest(String customerId, String cancellationReason) {
}
