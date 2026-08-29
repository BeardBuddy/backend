package com.beardbuddy.web.dto;

import com.beardbuddy.domain.enums.AppointmentStatus;

public final class AppointmentPatchRequests {

    private AppointmentPatchRequests() {
    }

    public record Cancel(String cancellationReason) {
    }

    public record Status(AppointmentStatus status) {
    }
}
