package com.beardbuddy.web.dto;

public record LoadBarbersResponse(
        String batchId,
        int published,
        String topic,
        String validFrom,
        String validTo,
        long tookMs
) {
}
