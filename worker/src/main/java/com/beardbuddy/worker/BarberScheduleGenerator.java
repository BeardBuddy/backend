package com.beardbuddy.worker;

import com.beardbuddy.domain.Schedule;
import com.beardbuddy.domain.User;
import com.beardbuddy.domain.enums.DayOfWeek;
import com.beardbuddy.domain.enums.SeniorityLevel;
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
 * Turns each BarberCreatedEvent into a barber row plus one Schedule row per requested weekday,
 * valid for the whole horizon carried on the event (a year by default).
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
                StructuredArguments.keyValue("validFrom", events.get(0).validFrom()),
                StructuredArguments.keyValue("validTo", events.get(0).validTo()));

        return schedulesCreated;
    }
}
