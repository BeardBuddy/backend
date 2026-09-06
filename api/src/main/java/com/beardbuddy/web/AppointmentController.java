package com.beardbuddy.web;

import com.beardbuddy.application.BookingService;
import com.beardbuddy.web.dto.AppointmentDto;
import com.beardbuddy.web.dto.AppointmentListDto;
import com.beardbuddy.web.dto.BookAppointmentRequest;
import com.beardbuddy.web.dto.CancelAppointmentRequest;
import com.beardbuddy.web.dto.CompleteAppointmentRequest;
import com.beardbuddy.web.dto.ReviewDto;
import com.beardbuddy.web.dto.SubmitReviewRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final BookingService bookingService;

    public AppointmentController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/customers/{customerId}/appointments")
    public AppointmentListDto appointments(@PathVariable String customerId) {
        return bookingService.appointmentsOfCustomer(customerId);
    }

    @GetMapping("/appointments/{id}")
    public AppointmentDto appointment(@PathVariable String id) {
        return bookingService.appointment(id);
    }

    @PostMapping("/appointments")
    public AppointmentDto book(@RequestBody BookAppointmentRequest request) {
        return bookingService.book(request);
    }

    @PatchMapping("/appointments/{id}/cancel")
    public AppointmentDto cancel(@PathVariable String id, @RequestBody CancelAppointmentRequest request) {
        return bookingService.cancel(id, request.customerId(), request.cancellationReason());
    }

    @PatchMapping("/appointments/{id}/complete")
    public AppointmentDto complete(@PathVariable String id, @RequestBody CompleteAppointmentRequest request) {
        return bookingService.complete(id, request.customerId());
    }

    @PostMapping("/appointments/{id}/review")
    public ReviewDto review(@PathVariable String id, @RequestBody SubmitReviewRequest request) {
        return bookingService.review(id, request.customerId(), request.rating(), request.comment());
    }
}
