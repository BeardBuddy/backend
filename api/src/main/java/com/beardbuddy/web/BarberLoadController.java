package com.beardbuddy.web;

import com.beardbuddy.application.BarberLoadService;
import com.beardbuddy.web.dto.LoadBarbersRequest;
import com.beardbuddy.web.dto.LoadBarbersResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Both endpoints require a signed-in user (see SecurityConfig) and share one per-user cooldown.
 * They publish one event per barber and return immediately: the worker materialises barbers and
 * schedules asynchronously, so a 202 means "accepted", not "done".
 */
@RestController
@RequestMapping("/api/barbers")
public class BarberLoadController {

    private final BarberLoadService barberLoadService;

    public BarberLoadController(BarberLoadService barberLoadService) {
        this.barberLoadService = barberLoadService;
    }

    /** One click in the UI: imports a default-sized batch of barbers with varied shifts. */
    @PostMapping("/import")
    public ResponseEntity<LoadBarbersResponse> importBarbers(@AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(barberLoadService.importBarbers(userId));
    }

    /** Custom import: caller picks count, hours and weekdays. */
    @PostMapping("/load")
    public ResponseEntity<LoadBarbersResponse> load(
            @AuthenticationPrincipal String userId,
            @RequestBody LoadBarbersRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(barberLoadService.load(userId, request));
    }
}
