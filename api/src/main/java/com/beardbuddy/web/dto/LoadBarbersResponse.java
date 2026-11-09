package com.beardbuddy.web.dto;

public record LoadBarbersResponse(
        String batchId,
        int published,
        String topic,
        String validFrom,
        String validTo,
        long tookMs,
        /** ISO-8601 instant from which the same user may import again. */
        String nextImportAt
) {
}
