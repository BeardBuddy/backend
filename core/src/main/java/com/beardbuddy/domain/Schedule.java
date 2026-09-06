package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.DayOfWeek;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule")
public class Schedule {

    private static final int SLOT_MINUTES = 30;

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barberId", nullable = false)
    private User barber;

    @Enumerated(EnumType.STRING)
    @Column(name = "dayOfWeek", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "startTime", nullable = false)
    private String startTime;

    @Column(name = "endTime", nullable = false)
    private String endTime;

    @Column(name = "validFrom", nullable = false)
    private String validFrom;

    @Column(name = "validTo", nullable = false)
    private String validTo;

    @Column(name = "isActive")
    private Integer isActive;

    protected Schedule() {
    }

    /**
     * Creates one weekly schedule row. Used by the worker when it materialises a barber's
     * calendar from the weekday + hours boundaries carried on a BarberCreatedEvent.
     */
    public static Schedule of(
            String id,
            User barber,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            LocalDate validFrom,
            LocalDate validTo
    ) {
        Schedule schedule = new Schedule();
        schedule.id = id;
        schedule.barber = barber;
        schedule.dayOfWeek = dayOfWeek;
        schedule.startTime = TimeSupport.format(startTime);
        schedule.endTime = TimeSupport.format(endTime);
        schedule.validFrom = validFrom.toString();
        schedule.validTo = validTo.toString();
        schedule.isActive = 1;
        return schedule;
    }

    public String getId() {
        return id;
    }

    public User getBarber() {
        return barber;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public boolean isActive() {
        return isActive != null && isActive != 0;
    }

    public double getTotalHours() {
        return (LocalTime.parse(endTime).toSecondOfDay() - LocalTime.parse(startTime).toSecondOfDay()) / 3600.0;
    }

    public boolean matchesDate(LocalDate date) {
        if (!isActive()) {
            return false;
        }
        if (DayOfWeek.from(date.getDayOfWeek()) != dayOfWeek) {
            return false;
        }
        return !date.isBefore(LocalDate.parse(validFrom)) && !date.isAfter(LocalDate.parse(validTo));
    }

    public boolean covers(LocalDate date, LocalTime time) {
        if (!matchesDate(date)) {
            return false;
        }
        LocalTime open = LocalTime.parse(startTime);
        LocalTime close = LocalTime.parse(endTime);
        return !time.isBefore(open) && time.isBefore(close);
    }

    public List<String> getRemainingSlots(List<Appointment> occupied, int serviceDuration) {
        LocalTime open = LocalTime.parse(startTime);
        LocalTime close = LocalTime.parse(endTime);
        List<String> slots = new ArrayList<>();

        for (LocalTime slot = open; !slot.plusMinutes(serviceDuration).isAfter(close); slot = slot.plusMinutes(SLOT_MINUTES)) {
            LocalTime slotEnd = slot.plusMinutes(serviceDuration);
            LocalTime current = slot;
            boolean conflict = occupied.stream().anyMatch(a -> a.overlaps(current, slotEnd));
            if (!conflict) {
                slots.add(TimeSupport.format(slot));
            }
        }
        return slots;
    }
}
