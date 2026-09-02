package com.beardbuddy.web.dto;

import java.util.List;

public record AppointmentListDto(
        List<AppointmentDto> upcoming,
        List<AppointmentDto> completed,
        List<AppointmentDto> cancelled
) {
}
