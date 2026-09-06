package com.beardbuddy.worker;

import com.beardbuddy.messaging.BarberCreatedEvent;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BarberCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(BarberCreatedListener.class);

    private final BarberScheduleGenerator generator;

    public BarberCreatedListener(BarberScheduleGenerator generator) {
        this.generator = generator;
    }

    @KafkaListener(topics = "${beardbuddy.kafka.barber-topic}")
    public void onBarbersCreated(List<BarberCreatedEvent> events) {
        if (events.isEmpty()) {
            return;
        }

        long startedAt = System.currentTimeMillis();
        try {
            int schedules = generator.generate(events);
            log.info("barbers.batch.consumed",
                    StructuredArguments.keyValue("event", "barbers.batch.consumed"),
                    StructuredArguments.keyValue("batchId", events.get(0).batchId()),
                    StructuredArguments.keyValue("barbers", events.size()),
                    StructuredArguments.keyValue("schedules", schedules),
                    StructuredArguments.keyValue("tookMs", System.currentTimeMillis() - startedAt));
        } catch (Exception e) {
            // Log and swallow: one poisoned batch must not stall the partition forever.
            log.error("barbers.batch.failed",
                    StructuredArguments.keyValue("event", "barbers.batch.failed"),
                    StructuredArguments.keyValue("batchId", events.get(0).batchId()),
                    StructuredArguments.keyValue("barbers", events.size()),
                    StructuredArguments.keyValue("error", e.getMessage()),
                    e);
        }
    }
}
