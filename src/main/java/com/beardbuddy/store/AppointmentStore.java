package com.beardbuddy.store;

import com.beardbuddy.domain.Appointment;
import com.beardbuddy.domain.AppointmentExtra;
import com.beardbuddy.domain.enums.AppointmentStatus;
import com.beardbuddy.repository.AppointmentRepository;
import com.beardbuddy.web.dto.AppointmentCreateRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AppointmentStore {

    @PersistenceContext
    private EntityManager entityManager;

    private final AppointmentRepository appointmentRepository;

    public AppointmentStore(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public void create(AppointmentCreateRequest req) {
        Appointment appointment = new Appointment(
                req.id(),
                req.customerId(),
                req.barberId(),
                req.serviceId(),
                req.date(),
                req.startTime(),
                req.endTime(),
                req.status(),
                req.paymentStatus(),
                req.paymentMethod(),
                req.totalPrice(),
                req.notes()
        );
        entityManager.persist(appointment);

        if (req.extraServiceIds() != null) {
            for (String extraServiceId : req.extraServiceIds()) {
                entityManager.persist(new AppointmentExtra(req.id(), extraServiceId));
            }
        }

        entityManager.flush();
    }

    @Transactional
    public void cancel(String id, String cancellationReason) {
        appointmentRepository.findById(id).ifPresent(appointment -> {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setCancellationReason(cancellationReason);
        });
    }

    @Transactional
    public void updateStatus(String id, AppointmentStatus status) {
        appointmentRepository.findById(id).ifPresent(appointment -> appointment.setStatus(status));
    }
}
