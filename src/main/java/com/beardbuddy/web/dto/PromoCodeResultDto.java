package com.beardbuddy.web.dto;

import com.beardbuddy.web.json.CompactDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public record PromoCodeResultDto(
        String code,
        Integer discountPercent,
        @JsonSerialize(using = CompactDoubleSerializer.class) Double discountAmount,
        @JsonSerialize(using = CompactDoubleSerializer.class) Double newTotal
) {
}
