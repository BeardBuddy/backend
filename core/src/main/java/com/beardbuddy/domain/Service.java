package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.CertificationLevel;
import com.beardbuddy.domain.enums.ServiceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service")
public class Service {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ServiceType type;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "description", nullable = false)
    private String description;

    @Column
    private Boolean isAvailable;

    @Column
    private Boolean requiresStyling;

    @Enumerated(EnumType.STRING)
    @Column
    private CertificationLevel complexityLevel;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "service_sub_service",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "sub_service_id")
    )
    private List<Service> subServices = new ArrayList<>();

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    private List<BarberService> barberServices = new ArrayList<>();

    protected Service() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public ServiceType getType() {
        return type;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailable() {
        return Boolean.TRUE.equals(isAvailable);
    }

    public Boolean getRequiresStyling() {
        return requiresStyling;
    }

    public CertificationLevel getComplexityLevel() {
        return complexityLevel;
    }

    public List<Service> getSubServices() {
        return subServices;
    }

    public List<String> getSubServiceIds() {
        return subServices.stream().map(Service::getId).toList();
    }

    public List<BarberService> getBarberServices() {
        return barberServices;
    }

    public List<User> getBarbers() {
        return barberServices.stream()
                .map(BarberService::getBarber)
                .toList();
    }

    public boolean isHaircut() {
        return type == ServiceType.HAIRCUT || includesType(ServiceType.HAIRCUT);
    }

    public boolean isBeard() {
        return type == ServiceType.BEARD || includesType(ServiceType.BEARD);
    }

    private boolean includesType(ServiceType wanted) {
        return type == ServiceType.HYBRID && subServices.stream().anyMatch(s -> s.getType() == wanted);
    }

    public List<ServiceType> getServiceTypes() {
        if (type != ServiceType.HYBRID) {
            return List.of(type);
        }
        return subServices.stream().map(Service::getType).distinct().toList();
    }

    public boolean requiresStyling() {
        return Boolean.TRUE.equals(requiresStyling);
    }

    public boolean includesTrim() {
        return isBeard();
    }

    public int estimateDuration() {
        if (type == ServiceType.HYBRID && !subServices.isEmpty()) {
            return subServices.stream()
                    .mapToInt(Service::getDuration)
                    .sum();
        }
        return duration;
    }
}
