package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.SeniorityLevel;
import com.beardbuddy.domain.enums.SpecializationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "barber_service",
        uniqueConstraints = @UniqueConstraint(name = "uk_barber_service_serviceId", columnNames = "serviceId")
)
public class BarberService {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barberId", nullable = false)
    private User barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceId", nullable = false, unique = true)
    private Service service;

    @Column(name = "barberId", insertable = false, updatable = false)
    private String barberId;

    @Column(name = "serviceId", insertable = false, updatable = false)
    private String serviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "seniority", nullable = false)
    private SeniorityLevel seniority;

    @Enumerated(EnumType.STRING)
    @Column(name = "specializationType", nullable = false)
    private SpecializationType specializationType;

    protected BarberService() {
    }

    public String getId() {
        return id;
    }

    public User getBarber() {
        return barber;
    }

    public Service getService() {
        return service;
    }

    public String getBarberId() {
        return barberId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public SeniorityLevel getSeniority() {
        return seniority;
    }

    public SpecializationType getSpecializationType() {
        return specializationType;
    }
}
