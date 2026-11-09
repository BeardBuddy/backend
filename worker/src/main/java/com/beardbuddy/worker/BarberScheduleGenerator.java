package com.beardbuddy.worker;

import com.beardbuddy.domain.BarberService;
import com.beardbuddy.domain.Schedule;
import com.beardbuddy.domain.Service;
import com.beardbuddy.domain.User;
import com.beardbuddy.domain.enums.CertificationLevel;
import com.beardbuddy.domain.enums.DayOfWeek;
import com.beardbuddy.domain.enums.SeniorityLevel;
import com.beardbuddy.domain.enums.ServiceType;
import com.beardbuddy.domain.enums.SpecializationType;
import com.beardbuddy.messaging.BarberCreatedEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Turns each BarberCreatedEvent into a barber row, links it to the services it can offer, and
 * adds one Schedule row per requested weekday, valid for the whole horizon carried on the event
 * (a year by default).
 *
 * <p>A weekly-recurring Schedule already covers every matching date in its validity range — see
 * Schedule.matchesDate — so a year of availability is a handful of rows, not 365.
 */
@Component
public class BarberScheduleGenerator {

    private static final Logger log = LoggerFactory.getLogger(BarberScheduleGenerator.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public int generate(List<BarberCreatedEvent> events) {
        int schedulesCreated = 0;
        int servicesLinked = 0;
        List<Service> catalogue = entityManager
                .createQuery("SELECT s FROM Service s", Service.class)
                .getResultList();

        for (BarberCreatedEvent event : events) {
            User barber = User.barber(
                    event.barberId(),
                    event.firstName(),
                    event.lastName(),
                    event.phone(),
                    event.dateOfBirth(),
                    SeniorityLevel.valueOf(event.seniorityLevel()),
                    SpecializationType.valueOf(event.specializationType()),
                    event.experienceYears()
            );
            entityManager.persist(barber);

            // Without a barber_service row the barber exists but is never offered for booking.
            for (Service service : catalogue) {
                if (canOffer(barber, service)) {
                    entityManager.persist(BarberService.of(
                            event.barberId() + "-" + service.getId(), barber, service));
                    servicesLinked++;
                }
            }

            LocalTime startTime = LocalTime.parse(event.startTime());
            LocalTime endTime = LocalTime.parse(event.endTime());
            LocalDate validFrom = LocalDate.parse(event.validFrom());
            LocalDate validTo = LocalDate.parse(event.validTo());

            for (String weekDay : event.weekDays()) {
                Schedule schedule = Schedule.of(
                        event.barberId() + "-" + weekDay,
                        barber,
                        DayOfWeek.valueOf(weekDay),
                        startTime,
                        endTime,
                        validFrom,
                        validTo
                );
                entityManager.persist(schedule);
                schedulesCreated++;
            }
        }

        entityManager.flush();
        entityManager.clear();

        log.info("barbers.schedules.generated",
                StructuredArguments.keyValue("event", "barbers.schedules.generated"),
                StructuredArguments.keyValue("batchId", events.get(0).batchId()),
                StructuredArguments.keyValue("barbers", events.size()),
                StructuredArguments.keyValue("schedules", schedulesCreated),
                StructuredArguments.keyValue("servicesLinked", servicesLinked),
                StructuredArguments.keyValue("validFrom", events.get(0).validFrom()),
                StructuredArguments.keyValue("validTo", events.get(0).validTo()));

        return schedulesCreated;
    }

    /**
     * A barber offers every available service of their specialization (HYBRID services count for
     * both). EXPERT-level services, such as the Hot Towel Royal Shave, go to SENIOR barbers only.
     */
    static boolean canOffer(User barber, Service service) {
        if (!service.isAvailable()) {
            return false;
        }
        boolean typeMatches = service.getType() == ServiceType.HYBRID
                || service.getType().name().equals(barber.getSpecializationType().name());
        boolean levelMatches = service.getComplexityLevel() != CertificationLevel.EXPERT
                || barber.getSeniorityLevel() == SeniorityLevel.SENIOR;
        return typeMatches && levelMatches;
    }
}
