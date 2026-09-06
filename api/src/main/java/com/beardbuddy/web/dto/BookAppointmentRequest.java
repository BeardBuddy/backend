package com.beardbuddy.web.dto;

import java.util.List;

public record BookAppointmentRequest(
        String customerId,
        String barberId,
        String serviceId,
        String date,
        String startTime,
        List<String> extraServiceIds,
        String promoCode,
        String notes
) {
}
