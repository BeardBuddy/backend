package com.beardbuddy.application;

import com.beardbuddy.domain.Appointment;
import com.beardbuddy.domain.ExtraService;
import com.beardbuddy.domain.PromoCode;
import com.beardbuddy.domain.Service;
import com.beardbuddy.domain.User;
import com.beardbuddy.domain.exception.DomainRuleException;
import com.beardbuddy.domain.exception.NotFoundException;
import com.beardbuddy.repository.AppointmentRepository;
import com.beardbuddy.repository.ExtraServiceRepository;
import com.beardbuddy.repository.ServiceRepository;
import com.beardbuddy.repository.UserRepository;
import com.beardbuddy.web.dto.AppointmentDto;
import com.beardbuddy.web.dto.AppointmentListDto;
import com.beardbuddy.web.dto.BookAppointmentRequest;
import com.beardbuddy.web.dto.ReviewDto;
import com.beardbuddy.domain.enums.AppointmentStatus;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ExtraServiceRepository extraServiceRepository;
    private final AppointmentRepository appointmentRepository;

    public BookingService(
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            ExtraServiceRepository extraServiceRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.extraServiceRepository = extraServiceRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public AppointmentDto book(BookAppointmentRequest request) {
        User customer = requireUser(request.customerId(), "Customer not found");
        User barber = requireUser(request.barberId(), "Barber not found");
        Service service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        LocalDate date = parseDate(request.date());
        LocalTime startTime = parseTime(request.startTime());

        String appointmentId = "appt-" + System.currentTimeMillis();
        Appointment appointment = customer.bookAppointment(appointmentId, barber, service, date, startTime);

        if (request.extraServiceIds() != null) {
            for (String extraId : request.extraServiceIds()) {
                ExtraService extra = extraServiceRepository.findById(extraId)
                        .orElseThrow(() -> new NotFoundException("Extra service not found: " + extraId));
                appointment.addExtraService(extra);
            }
        }

        appointment.addNote(request.notes());

        if (request.promoCode() != null && !request.promoCode().isBlank()) {
            PromoCode promoCode = PromoCode.findByCode(request.promoCode())
                    .filter(PromoCode::isValid)
                    .orElseThrow(() -> new DomainRuleException("Code cannot be applied"));
            appointment.applyDiscount(promoCode.calculateDiscount(appointment.getTotalPrice()));
        }

        barber.confirmAppointment(appointment);

        entityManager.persist(appointment);
        entityManager.flush();

        log.info("appointment.created",
                StructuredArguments.keyValue("event", "appointment.created"),
                StructuredArguments.keyValue("appointmentId", appointment.getId()),
                StructuredArguments.keyValue("customerId", customer.getId()),
                StructuredArguments.keyValue("barberId", barber.getId()),
                StructuredArguments.keyValue("serviceId", service.getId()),
                StructuredArguments.keyValue("serviceName", service.getName()),
                StructuredArguments.keyValue("date", appointment.getDate()),
                StructuredArguments.keyValue("startTime", appointment.getStartTime()),
                StructuredArguments.keyValue("endTime", appointment.getEndTime()),
                StructuredArguments.keyValue("totalPrice", appointment.getTotalPrice()),
                StructuredArguments.keyValue("extras", appointment.getExtraServices().size()),
                StructuredArguments.keyValue("promoApplied", request.promoCode() != null));

        return AppointmentDto.from(appointment);
    }

    @Transactional
    public AppointmentDto cancel(String appointmentId, String customerId, String reason) {
        User customer = requireUser(customerId, "Customer not found");
        Appointment appointment = requireAppointment(appointmentId);

        customer.cancelAppointment(appointment, reason);
        entityManager.flush();

        return AppointmentDto.from(appointment);
    }

    @Transactional
    public AppointmentDto complete(String appointmentId, String customerId) {
        User customer = requireUser(customerId, "Customer not found");
        Appointment appointment = requireAppointment(appointmentId);

        customer.completeOwnAppointment(appointment);
        entityManager.flush();

        return AppointmentDto.from(appointment);
    }

    @Transactional
    public ReviewDto review(String appointmentId, String customerId, Integer rating, String comment) {
        if (rating == null) {
            throw new DomainRuleException("Rating is required");
        }
        User customer = requireUser(customerId, "Customer not found");
        Appointment appointment = requireAppointment(appointmentId);

        var review = customer.submitReview(appointment, rating, comment, LocalDate.now());
        entityManager.flush();

        return ReviewDto.from(review);
    }

    @Transactional(readOnly = true)
    public AppointmentListDto appointmentsOfCustomer(String customerId) {
        User customer = requireUser(customerId, "Customer not found");

        List<Appointment> all = customer.getBookedAppointments();

        return new AppointmentListDto(
                all.stream().filter(Appointment::isUpcoming).map(AppointmentDto::from).toList(),
                all.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).map(AppointmentDto::from).toList(),
                all.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).map(AppointmentDto::from).toList()
        );
    }

    @Transactional(readOnly = true)
    public AppointmentDto appointment(String appointmentId) {
        return AppointmentDto.from(requireAppointment(appointmentId));
    }

    private User requireUser(String id, String message) {
        if (id == null || id.isBlank()) {
            throw new DomainRuleException(message);
        }
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(message));
    }

    private Appointment requireAppointment(String id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new DomainRuleException("Invalid date, expected YYYY-MM-DD");
        }
    }

    private static LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value);
        } catch (Exception e) {
            throw new DomainRuleException("Invalid time, expected HH:mm");
        }
    }
}
