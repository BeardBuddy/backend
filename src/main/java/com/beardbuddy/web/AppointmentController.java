package com.beardbuddy.web;

import com.beardbuddy.store.AppointmentStore;
import com.beardbuddy.web.dto.AppointmentCreateRequest;
import com.beardbuddy.web.dto.AppointmentPatchRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentStore appointmentStore;

    public AppointmentController(AppointmentStore appointmentStore) {
        this.appointmentStore = appointmentStore;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AppointmentCreateRequest request) {
        try {
            appointmentStore.create(request);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("[POST /api/appointments]", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create appointment"));
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable String id,
            @RequestBody(required = false) AppointmentPatchRequests.Cancel request
    ) {
        try {
            appointmentStore.cancel(id, request == null ? null : request.cancellationReason());
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("[PATCH /api/appointments/{}/cancel]", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cancel appointment"));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestBody AppointmentPatchRequests.Status request
    ) {
        try {
            appointmentStore.updateStatus(id, request.status());
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("[PATCH /api/appointments/{}/status]", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update status"));
        }
    }
}
