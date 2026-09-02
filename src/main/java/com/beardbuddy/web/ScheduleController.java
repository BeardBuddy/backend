package com.beardbuddy.web;

import com.beardbuddy.application.ScheduleService;
import com.beardbuddy.domain.exception.DomainRuleException;
import com.beardbuddy.web.dto.AvailableSlotsDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/barbers/{barberId}")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/slots")
    public AvailableSlotsDto slots(
            @PathVariable String barberId,
            @RequestParam String serviceId,
            @RequestParam String date,
            @RequestParam(required = false) String customerId
    ) {
        return scheduleService.availableSlots(barberId, serviceId, customerId, parseDate(date));
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new DomainRuleException("Invalid date, expected YYYY-MM-DD");
        }
    }
}
