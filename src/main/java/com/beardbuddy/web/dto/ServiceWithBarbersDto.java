package com.beardbuddy.web.dto;

import com.beardbuddy.domain.Service;
import com.beardbuddy.domain.enums.CertificationLevel;
import com.beardbuddy.domain.enums.ServiceType;
import com.beardbuddy.web.json.CompactDoubleSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

public record ServiceWithBarbersDto(
        String id,
        String name,
        @JsonSerialize(using = CompactDoubleSerializer.class) Double price,
        ServiceType type,
        Integer duration,
        String description,
        @JsonProperty("isAvailable") boolean isAvailable,
        Boolean requiresStyling,
        CertificationLevel complexityLevel,
        List<String> subServiceIds,
        List<UserDto> barbers
) {

    public static ServiceWithBarbersDto from(Service s, List<UserDto> barbers) {
        return new ServiceWithBarbersDto(
                s.getId(),
                s.getName(),
                s.getPrice(),
                s.getType(),
                s.getDuration(),
                s.getDescription(),
                Boolean.TRUE.equals(s.getIsAvailable()),
                s.getRequiresStyling(),
                s.getComplexityLevel(),
                s.getSubServiceIds(),
                barbers
        );
    }
}
