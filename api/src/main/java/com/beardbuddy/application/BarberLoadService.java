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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fabricates barbers and publishes one BarberCreatedEvent each to the barber.created topic.
 * Nothing is written to the database here — the worker owns that (barber row, service links,
 * schedules), so the request returns immediately.
 *
 * <p>Every import is guarded by a per-user cooldown, so a registered user cannot flood the topic.
 */
@Component
public class BarberLoadService {

    private static final Logger log = LoggerFactory.getLogger(BarberLoadService.class);

    private static final List<String> FIRST_NAMES = List.of(
            "Marcus", "Elena", "Leo", "Viktor", "Nadia", "Oskar", "Iris", "Tomas",
            "Sofia", "Hugo", "Lena", "Piotr", "Maja", "Anton", "Klara", "Emil"
    );
    private static final List<String> LAST_NAMES = List.of(
            "Vance", "Rostova", "Sterling", "Kael", "Novak", "Lindqvist", "Moreau", "Bauer",
            "Kowalski", "Ferreira", "Adeyemi", "Nakamura", "Olsen", "Duarte", "Haas", "Petrov"
    );

    /** Shifts handed out by the one-click import, so imported barbers do not all share one window. */
    private static final List<Shift> SHIFTS = List.of(
            new Shift(LocalTime.of(9, 0), LocalTime.of(17, 0), List.of("MON", "TUE", "WED", "THU", "FRI")),
            new Shift(LocalTime.of(10, 0), LocalTime.of(18, 0), List.of("TUE", "WED", "THU", "FRI", "SAT")),
            new Shift(LocalTime.of(12, 0), LocalTime.of(20, 0), List.of("MON", "TUE", "WED", "THU", "FRI")),
            new Shift(LocalTime.of(9, 0), LocalTime.of(14, 0), List.of("MON", "WED", "FRI", "SAT")),
            new Shift(LocalTime.of(14, 0), LocalTime.of(21, 0), List.of("TUE", "THU", "SAT", "SUN"))
    );

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BarberImportCooldown cooldown;
    private final String topic;
    private final int horizonDays;
    private final int batchSize;
    private final int maxBatchSize;

    public BarberLoadService(
            KafkaTemplate<String, Object> kafkaTemplate,
            BarberImportCooldown cooldown,
            @Value("${beardbuddy.kafka.barber-topic}") String topic,
            @Value("${beardbuddy.schedule.horizon-days}") int horizonDays,
            @Value("${beardbuddy.barber-import.batch-size}") int batchSize,
            @Value("${beardbuddy.barber-import.max-batch-size}") int maxBatchSize
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.cooldown = cooldown;
        this.topic = topic;
        this.horizonDays = horizonDays;
        this.batchSize = batchSize;
        this.maxBatchSize = maxBatchSize;
    }

    /** The "Import barbers" button: a fixed-size batch with varied, realistic shifts. */
    public LoadBarbersResponse importBarbers(String userId) {
        Instant nextImportAt = cooldown.claim(userId);

        LocalDate validFrom = LocalDate.now();
        LocalDate validTo = validFrom.plusDays(horizonDays);
        String batchId = UUID.randomUUID().toString();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        return publish(userId, batchId, batchSize, validFrom, validTo, nextImportAt, index -> {
            Shift shift = SHIFTS.get(random.nextInt(SHIFTS.size()));
            return fabricate(batchId, index, shift, validFrom, validTo);
        });
    }

    /** Custom load: caller picks the size, hours and weekdays. Same cooldown as the button. */
    public LoadBarbersResponse load(String userId, LoadBarbersRequest request) {
        if (request == null) {
            throw new DomainRuleException("Request body is required");
        }
        int count = request.count() == null ? 0 : request.count();
        if (count < 1 || count > maxBatchSize) {
            throw new DomainRuleException("count must be between 1 and " + maxBatchSize);
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
        List<String> normalizedDays = weekDays.stream().map(day -> day.toUpperCase(Locale.ROOT)).toList();

        LocalDate validFrom = request.validFrom() == null ? LocalDate.now() : parseDate(request.validFrom());
        LocalDate validTo = validFrom.plusDays(horizonDays);

        // Validate first, so a typo does not burn the user's cooldown.
        Instant nextImportAt = cooldown.claim(userId);

        String batchId = UUID.randomUUID().toString();
        Shift shift = new Shift(startTime, endTime, normalizedDays);
        return publish(userId, batchId, count, validFrom, validTo, nextImportAt,
                index -> fabricate(batchId, index, shift, validFrom, validTo));
    }

    private LoadBarbersResponse publish(
            String userId,
            String batchId,
            int count,
            LocalDate validFrom,
            LocalDate validTo,
            Instant nextImportAt,
            java.util.function.IntFunction<BarberCreatedEvent> factory
    ) {
        long startedAt = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            BarberCreatedEvent event = factory.apply(i);
            // Key by barberId so all events for one barber land on the same partition.
            kafkaTemplate.send(topic, event.barberId(), event);
        }
        kafkaTemplate.flush();

        long tookMs = System.currentTimeMillis() - startedAt;

        log.info("barbers.load.published",
                StructuredArguments.keyValue("event", "barbers.load.published"),
                StructuredArguments.keyValue("batchId", batchId),
                StructuredArguments.keyValue("requestedBy", userId),
                StructuredArguments.keyValue("count", count),
                StructuredArguments.keyValue("horizonDays", horizonDays),
                StructuredArguments.keyValue("tookMs", tookMs));

        return new LoadBarbersResponse(
                batchId, count, topic, validFrom.toString(), validTo.toString(), tookMs, nextImportAt.toString());
    }

    private BarberCreatedEvent fabricate(
            String batchId,
            int index,
            Shift shift,
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
                shift.start().toString(),
                shift.end().toString(),
                shift.weekDays(),
                validFrom.toString(),
                validTo.toString()
        );
    }

    private static void validateWeekDay(String day) {
        try {
            com.beardbuddy.domain.enums.DayOfWeek.valueOf(day.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
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

    private record Shift(LocalTime start, LocalTime end, List<String> weekDays) {
    }
}
