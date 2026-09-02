package com.beardbuddy.application;

import com.beardbuddy.domain.Appointment;
import com.beardbuddy.domain.Service;
import com.beardbuddy.domain.User;
import com.beardbuddy.domain.exception.NotFoundException;
import com.beardbuddy.repository.ServiceRepository;
import com.beardbuddy.repository.UserRepository;
import com.beardbuddy.web.dto.AvailableSlotsDto;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduleService {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    public ScheduleService(UserRepository userRepository, ServiceRepository serviceRepository) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public AvailableSlotsDto availableSlots(String barberId, String serviceId, String customerId, LocalDate date) {
        User barber = userRepository.findById(barberId)
                .orElseThrow(() -> new NotFoundException("Barber not found"));
        if (!barber.isBarber()) {
            throw new NotFoundException("Barber not found");
        }
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        int duration = service.estimateDuration();

        List<Appointment> occupied = new ArrayList<>(activeAppointmentsOn(barber, date));

        if (customerId != null && !customerId.isBlank()) {
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new NotFoundException("Customer not found"));
            occupied.addAll(activeAppointmentsOn(customer, date));
        }

        List<String> slots = barber.getSchedules().stream()
                .filter(schedule -> schedule.matchesDate(date))
                .findFirst()
                .map(schedule -> schedule.getRemainingSlots(occupied, duration))
                .orElseGet(List::of);

        return new AvailableSlotsDto(barberId, serviceId, date.toString(), duration, slots);
    }

    private static List<Appointment> activeAppointmentsOn(User user, LocalDate date) {
        return user.getAppointmentsForDay(date).stream()
                .filter(Appointment::isActive)
                .toList();
    }
}
