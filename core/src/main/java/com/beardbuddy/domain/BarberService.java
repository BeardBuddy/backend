package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.CertificationLevel;
import com.beardbuddy.domain.enums.SeniorityLevel;
import com.beardbuddy.domain.enums.SpecializationType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.List;

@Entity
@Table(
        name = "barber_service",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_barber_service_barber_service",
                columnNames = {"barber_id", "serviceId"}
        )
)
public class BarberService {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barber_id", nullable = false)
    private User barber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Enumerated(EnumType.STRING)
    @Column(name = "seniority", nullable = false)
    private SeniorityLevel seniority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpecializationType specializationType;

    @Column
    private Integer yearsOfExperience;

    @Enumerated(EnumType.STRING)
    @Column
    private CertificationLevel certificationLevel;

    @Convert(converter = StringListJsonConverter.class)
    @Column
    private List<String> coursesCompleted;

    @Column
    private String acquiredAt;

    @Column(name = "notes")
    private String notes;

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

    public SeniorityLevel getSeniority() {
        return seniority;
    }

    public SpecializationType getSpecializationType() {
        return specializationType;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public CertificationLevel getCertificationLevel() {
        return certificationLevel;
    }

    public List<String> getCoursesCompleted() {
        return coursesCompleted == null ? List.of() : coursesCompleted;
    }

    public String getAcquiredAt() {
        return acquiredAt;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isExpert() {
        return seniority == SeniorityLevel.SENIOR && barber.isExpert();
    }
}
