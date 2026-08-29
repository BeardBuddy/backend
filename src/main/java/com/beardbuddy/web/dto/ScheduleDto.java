package com.beardbuddy.web.dto;

import com.beardbuddy.domain.Schedule;
import com.beardbuddy.domain.enums.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ScheduleDto(
        String id,
        String barberId,
        DayOfWeek dayOfWeek,
        String startTime,
        String endTime,
        String validFrom,
        String validTo,
        @JsonProperty("isActive") Integer isActive
) {

    public static ScheduleDto from(Schedule s) {
        return new ScheduleDto(
                s.getId(),
                s.getBarberId(),
                s.getDayOfWeek(),
                s.getStartTime(),
                s.getEndTime(),
                s.getValidFrom(),
                s.getValidTo(),
                s.getIsActive()
        );
    }
}
