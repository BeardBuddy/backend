package com.beardbuddy.web.dto;

import com.beardbuddy.domain.Appointment;
import com.beardbuddy.domain.enums.AppointmentStatus;
import com.beardbuddy.domain.enums.PaymentMethod;
import com.beardbuddy.domain.enums.PaymentStatus;
import com.beardbuddy.web.json.CompactDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public record AppointmentDto(
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
        @JsonSerialize(using = CompactDoubleSerializer.class) Double totalPrice,
        String notes,
        String cancellationReason
) {

    public static AppointmentDto from(Appointment a) {
        return new AppointmentDto(
                a.getId(),
                a.getCustomerId(),
                a.getBarberId(),
                a.getServiceId(),
                a.getDate(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                a.getPaymentStatus(),
                a.getPaymentMethod(),
                a.getTotalPrice(),
                a.getNotes(),
                a.getCancellationReason()
        );
    }
}
