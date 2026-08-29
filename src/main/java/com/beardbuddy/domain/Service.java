package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.CertificationLevel;
import com.beardbuddy.domain.enums.ServiceType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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

    @Column(name = "isAvailable")
    private Boolean isAvailable;

    @Column(name = "requiresStyling")
    private Boolean requiresStyling;

    @Enumerated(EnumType.STRING)
    @Column(name = "complexityLevel")
    private CertificationLevel complexityLevel;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "subServiceIds")
    private List<String> subServiceIds;

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

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public Boolean getRequiresStyling() {
        return requiresStyling;
    }

    public CertificationLevel getComplexityLevel() {
        return complexityLevel;
    }

    public List<String> getSubServiceIds() {
        return subServiceIds == null ? List.of() : subServiceIds;
    }

    public List<BarberService> getBarberServices() {
        return barberServices;
    }

    public List<User> getBarbers() {
        return barberServices.stream()
                .map(BarberService::getBarber)
                .toList();
    }
}
