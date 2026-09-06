package com.beardbuddy.web.dto;

import java.util.List;

public record AvailableSlotsDto(
        String barberId,
        String serviceId,
        String date,
        Integer durationMinutes,
        List<String> slots
) {
}
