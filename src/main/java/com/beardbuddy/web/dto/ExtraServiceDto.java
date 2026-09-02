package com.beardbuddy.web.dto;

import com.beardbuddy.domain.ExtraService;
import com.beardbuddy.web.json.CompactDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public record ExtraServiceDto(
        String id,
        String type,
        String name,
        @JsonSerialize(using = CompactDoubleSerializer.class) Double price,
        String description
) {

    public static ExtraServiceDto from(ExtraService extra) {
        return new ExtraServiceDto(
                extra.getId(),
                extra.getType().name(),
                extra.getName(),
                extra.getPrice(),
                extra.getDescription()
        );
    }
}
