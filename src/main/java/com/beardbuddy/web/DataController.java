package com.beardbuddy.web;

import com.beardbuddy.domain.AppointmentExtra;
import com.beardbuddy.repository.AppointmentExtraRepository;
import com.beardbuddy.repository.AppointmentRepository;
import com.beardbuddy.repository.BarberServiceRepository;
import com.beardbuddy.repository.ExtraServiceRepository;
import com.beardbuddy.repository.ReviewRepository;
import com.beardbuddy.repository.ScheduleRepository;
import com.beardbuddy.repository.ServiceRepository;
import com.beardbuddy.repository.UserRepository;
import com.beardbuddy.web.dto.AppointmentDto;
import com.beardbuddy.web.dto.BarberServiceDto;
import com.beardbuddy.web.dto.DataPayloadDto;
import com.beardbuddy.web.dto.ExtraServiceDto;
import com.beardbuddy.web.dto.ReviewDto;
import com.beardbuddy.web.dto.ScheduleDto;
import com.beardbuddy.web.dto.ServiceDto;
import com.beardbuddy.web.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DataController {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BarberServiceRepository barberServiceRepository;
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final ExtraServiceRepository extraServiceRepository;
    private final ReviewRepository reviewRepository;
    private final AppointmentExtraRepository appointmentExtraRepository;

    public DataController(
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            BarberServiceRepository barberServiceRepository,
            ScheduleRepository scheduleRepository,
            AppointmentRepository appointmentRepository,
            ExtraServiceRepository extraServiceRepository,
            ReviewRepository reviewRepository,
            AppointmentExtraRepository appointmentExtraRepository
    ) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.barberServiceRepository = barberServiceRepository;
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.extraServiceRepository = extraServiceRepository;
        this.reviewRepository = reviewRepository;
        this.appointmentExtraRepository = appointmentExtraRepository;
    }

    @GetMapping("/api/data")
    public ResponseEntity<?> getData() {
        try {
            List<UserDto> users = userRepository.findAll().stream().map(UserDto::from).toList();
            List<ServiceDto> services = serviceRepository.findAll().stream().map(ServiceDto::from).toList();
            List<BarberServiceDto> barberServices =
                    barberServiceRepository.findAll().stream().map(BarberServiceDto::from).toList();
            List<ScheduleDto> schedules = scheduleRepository.findAll().stream().map(ScheduleDto::from).toList();
            List<AppointmentDto> appointments =
                    appointmentRepository.findAll().stream().map(AppointmentDto::from).toList();
            List<ExtraServiceDto> extraServices =
                    extraServiceRepository.findAll().stream().map(ExtraServiceDto::from).toList();
            List<ReviewDto> reviews = reviewRepository.findAll().stream().map(ReviewDto::from).toList();

            Map<String, List<String>> appointmentExtras = new LinkedHashMap<>();
            for (AppointmentExtra link : appointmentExtraRepository.findAll()) {
                appointmentExtras
                        .computeIfAbsent(link.getAppointmentId(), key -> new ArrayList<>())
                        .add(link.getExtraServiceId());
            }

            return ResponseEntity.ok(new DataPayloadDto(
                    users,
                    services,
                    barberServices,
                    schedules,
                    appointments,
                    extraServices,
                    reviews,
                    appointmentExtras
            ));
        } catch (Exception e) {
            log.error("[GET /api/data]", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load data"));
        }
    }
}
