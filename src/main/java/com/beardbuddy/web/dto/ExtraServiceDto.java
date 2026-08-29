package com.beardbuddy.web.dto;

import com.beardbuddy.domain.ExtraService;
import com.beardbuddy.domain.enums.ExtraType;
import com.beardbuddy.web.json.CompactDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public record ExtraServiceDto(
        String id,
        ExtraType type,
        String name,
        @JsonSerialize(using = CompactDoubleSerializer.class) Double price,
        String description
) {

    public static ExtraServiceDto from(ExtraService e) {
        return new ExtraServiceDto(e.getId(), e.getType(), e.getName(), e.getPrice(), e.getDescription());
    }
}
