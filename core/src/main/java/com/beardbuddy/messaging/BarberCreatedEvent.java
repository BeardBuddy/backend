package com.beardbuddy.messaging;

import java.util.List;

/**
 * Published by the API when a barber is loaded, consumed by the worker which turns the weekday +
 * hours boundaries into concrete Schedule rows.
 *
 * <p>Lives in core because both sides must agree on the shape.
 */
public record BarberCreatedEvent(
        String batchId,
        String barberId,
        String firstName,
        String lastName,
        String phone,
        String dateOfBirth,
        String seniorityLevel,
        String specializationType,
        Integer experienceYears,
        String startTime,
        String endTime,
        List<String> weekDays,
        String validFrom,
        String validTo
) {
}
