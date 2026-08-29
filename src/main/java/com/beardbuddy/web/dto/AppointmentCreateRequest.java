package com.beardbuddy.web.dto;

import com.beardbuddy.domain.enums.AppointmentStatus;
import com.beardbuddy.domain.enums.PaymentMethod;
import com.beardbuddy.domain.enums.PaymentStatus;

import java.util.List;

public record AppointmentCreateRequest(
        String id,
        String customerId,
        String barberId,
        String serviceId,
        String date,
        String startTime,
        String endTime,
        AppointmentStatus status,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        Double totalPrice,
        String notes,
        List<String> extraServiceIds
) {
}
