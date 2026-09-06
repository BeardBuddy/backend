package com.beardbuddy.application;

import com.beardbuddy.domain.exception.DomainRuleException;
import com.beardbuddy.messaging.BarberCreatedEvent;
import com.beardbuddy.web.dto.LoadBarbersRequest;
import com.beardbuddy.web.dto.LoadBarbersResponse;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fabricates barbers and publishes one BarberCreatedEvent each. Nothing is written to the database
 * here — the worker owns that, so a load of 5000 returns immediately instead of blocking on
 * ~1.3M schedule inserts.
 */
@Component
public class BarberLoadService {

    private static final Logger log = LoggerFactory.getLogger(BarberLoadService.class);

    private static final int MAX_BATCH = 20_000;

    private static final List<String> FIRST_NAMES = List.of(
            "Marcus", "Elena", "Leo", "Viktor", "Nadia", "Oskar", "Iris", "Tomas",
            "Sofia", "Hugo", "Lena", "Piotr", "Maja", "Anton", "Klara", "Emil"
    );
    private static final List<String> LAST_NAMES = List.of(
            "Vance", "Rostova", "Sterling", "Kael", "Novak", "Lindqvist", "Moreau", "Bauer",
            "Kowalski", "Ferreira", "Adeyemi", "Nakamura", "Olsen", "Duarte", "Haas", "Petrov"
    );

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;
    private final int horizonDays;

    public BarberLoadService(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${beardbuddy.kafka.barber-topic}") String topic,
            @Value("${beardbuddy.schedule.horizon-days}") int horizonDays
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.horizonDays = horizonDays;
    }

    public LoadBarbersResponse load(LoadBarbersRequest request) {
        int count = request.count() == null ? 0 : request.count();
        if (count < 1 || count > MAX_BATCH) {
            throw new DomainRuleException("count must be between 1 and " + MAX_BATCH);
        }

        LocalTime startTime = parseTime(request.startTime(), "startTime");
        LocalTime endTime = parseTime(request.endTime(), "endTime");
        if (!startTime.isBefore(endTime)) {
            throw new DomainRuleException("startTime must be before endTime");
        }

        List<String> weekDays = request.weekDays();
        if (weekDays == null || weekDays.isEmpty()) {
            throw new DomainRuleException("weekDays must not be empty");
        }
        weekDays.forEach(BarberLoadService::validateWeekDay);

        LocalDate validFrom = request.validFrom() == null ? LocalDate.now() : parseDate(request.validFrom());
        LocalDate validTo = validFrom.plusDays(horizonDays);

        String batchId = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            BarberCreatedEvent event = fabricate(batchId, i, startTime, endTime, weekDays, validFrom, validTo);
            // Key by barberId so all events for one barber land on the same partition.
            kafkaTemplate.send(topic, event.barberId(), event);
        }
        kafkaTemplate.flush();

        long tookMs = System.currentTimeMillis() - startedAt;

        log.info("barbers.load.published",
                StructuredArguments.keyValue("event", "barbers.load.published"),
                StructuredArguments.keyValue("batchId", batchId),
                StructuredArguments.keyValue("count", count),
                StructuredArguments.keyValue("weekDays", String.join(",", weekDays)),
                StructuredArguments.keyValue("startTime", request.startTime()),
                StructuredArguments.keyValue("endTime", request.endTime()),
                StructuredArguments.keyValue("horizonDays", horizonDays),
                StructuredArguments.keyValue("tookMs", tookMs));

        return new LoadBarbersResponse(batchId, count, topic, validFrom.toString(), validTo.toString(), tookMs);
    }

    private BarberCreatedEvent fabricate(
            String batchId,
            int index,
            LocalTime startTime,
            LocalTime endTime,
            List<String> weekDays,
            LocalDate validFrom,
            LocalDate validTo
    ) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String firstName = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
        String lastName = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));

        boolean senior = random.nextBoolean();
        String seniority = senior ? "SENIOR" : "JUNIOR";
        String specialization = random.nextBoolean() ? "HAIRCUT" : "BEARD";
        int experienceYears = senior ? random.nextInt(5, 20) : random.nextInt(1, 5);

        return new BarberCreatedEvent(
                batchId,
                "barber-" + batchId.substring(0, 8) + "-" + index,
                firstName,
                lastName,
                "+4870000%04d".formatted(index % 10_000),
                LocalDate.of(1980, 1, 1).plusDays(random.nextInt(0, 8000)).toString(),
                seniority,
                specialization,
                experienceYears,
                startTime.toString(),
                endTime.toString(),
                weekDays.stream().map(day -> day.toUpperCase(Locale.ROOT)).toList(),
                validFrom.toString(),
                validTo.toString()
        );
    }

    private static void validateWeekDay(String day) {
        try {
            com.beardbuddy.domain.enums.DayOfWeek.valueOf(day.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new DomainRuleException("Invalid weekday: " + day + " (expected MON..SUN)");
        }
    }

    private static LocalTime parseTime(String value, String field) {
        try {
            return LocalTime.parse(value);
        } catch (Exception e) {
            throw new DomainRuleException("Invalid " + field + ", expected HH:mm");
        }
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new DomainRuleException("Invalid validFrom, expected YYYY-MM-DD");
        }
    }
}
