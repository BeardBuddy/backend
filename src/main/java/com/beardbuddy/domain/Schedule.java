package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.DayOfWeek;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "barberId", nullable = false)
    private String barberId;

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

    public String getId() {
        return id;
    }

    public String getBarberId() {
        return barberId;
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

    public Integer getIsActive() {
        return isActive;
    }
}
