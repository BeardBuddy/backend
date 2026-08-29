package com.beardbuddy.web.dto;

import java.util.List;
import java.util.Map;

public record DataPayloadDto(
        List<UserDto> users,
        List<ServiceDto> services,
        List<BarberServiceDto> barberServices,
        List<ScheduleDto> schedules,
        List<AppointmentDto> appointments,
        List<ExtraServiceDto> extraServices,
        List<ReviewDto> reviews,
        Map<String, List<String>> appointmentExtras
) {
}
