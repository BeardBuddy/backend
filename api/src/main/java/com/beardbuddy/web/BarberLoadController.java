package com.beardbuddy.web;

import com.beardbuddy.application.BarberLoadService;
import com.beardbuddy.web.dto.LoadBarbersRequest;
import com.beardbuddy.web.dto.LoadBarbersResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/barbers")
public class BarberLoadController {

    private final BarberLoadService barberLoadService;

    public BarberLoadController(BarberLoadService barberLoadService) {
        this.barberLoadService = barberLoadService;
    }

    /**
     * Publishes one event per barber and returns immediately. Schedules are materialised
     * asynchronously by the worker, so a 202 here means "accepted", not "done".
     */
    @PostMapping("/load")
    public ResponseEntity<LoadBarbersResponse> load(@RequestBody LoadBarbersRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(barberLoadService.load(request));
    }
}
