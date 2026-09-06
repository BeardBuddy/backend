package com.beardbuddy.web.dto;

import java.util.List;

public record LoadBarbersRequest(
        Integer count,
        String startTime,
        String endTime,
        List<String> weekDays,
        String validFrom
) {
}
