package com.beardbuddy.web.dto;

import com.beardbuddy.domain.Appointment;
import com.beardbuddy.web.json.CompactDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

public record AppointmentDto(
        String id,
        String customerId,
        String barberId,
        String barberName,
        String serviceId,
        String serviceName,
        String date,
        String startTime,
        String endTime,
        String status,
        String paymentStatus,
        String paymentMethod,
        @JsonSerialize(using = CompactDoubleSerializer.class) Double totalPrice,
        List<String> notes,
        String cancellationReason,
        String paidAt,
        String cancelledAt,
        List<ExtraServiceDto> extraServices,
        boolean canBeReviewed,
        boolean canBeCancelled,
        ReviewDto review
) {

    public static AppointmentDto from(Appointment appointment) {
        return new AppointmentDto(
                appointment.getId(),
                appointment.getCustomer().getId(),
                appointment.getBarber().getId(),
                appointment.getBarber().getFullName(),
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus().name(),
                appointment.getPaymentStatus().name(),
                appointment.getPaymentMethod().name(),
                appointment.getTotalPrice(),
                appointment.getNotes(),
                appointment.getCancellationReason(),
                appointment.getPaidAt(),
                appointment.getCancelledAt(),
                appointment.getExtraServices().stream().map(ExtraServiceDto::from).toList(),
                appointment.canBeReviewed(),
                appointment.isUpcoming(),
                appointment.getReview() == null ? null : ReviewDto.from(appointment.getReview())
        );
    }
}
