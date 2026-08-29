package com.beardbuddy.web.dto;

import com.beardbuddy.domain.BarberService;
import com.beardbuddy.domain.enums.SeniorityLevel;
import com.beardbuddy.domain.enums.SpecializationType;

public record BarberServiceDto(
        String id,
        String barberId,
        String serviceId,
        SeniorityLevel seniority,
        SpecializationType specializationType
) {

    public static BarberServiceDto from(BarberService bs) {
        return new BarberServiceDto(
                bs.getId(),
                bs.getBarberId(),
                bs.getServiceId(),
                bs.getSeniority(),
                bs.getSpecializationType()
        );
    }
}
