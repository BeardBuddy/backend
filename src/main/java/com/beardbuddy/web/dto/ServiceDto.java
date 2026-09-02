package com.beardbuddy.web.dto;

import com.beardbuddy.domain.Service;
import com.beardbuddy.web.json.CompactDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

public record ServiceDto(
        String id,
        String name,
        @JsonSerialize(using = CompactDoubleSerializer.class) Double price,
        String type,
        Integer duration,
        String description,
        boolean available,
        String complexityLevel,
        List<String> subServiceIds,
        List<BarberDto> barbers
) {

    public static ServiceDto from(Service service) {
        return new ServiceDto(
                service.getId(),
                service.getName(),
                service.getPrice(),
                service.getType().name(),
                service.estimateDuration(),
                service.getDescription(),
                service.isAvailable(),
                service.getComplexityLevel() == null ? null : service.getComplexityLevel().name(),
                service.getSubServiceIds(),
                service.getBarbers().stream().map(BarberDto::from).toList()
        );
    }
}
