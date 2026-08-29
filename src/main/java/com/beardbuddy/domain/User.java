package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.SeniorityLevel;
import com.beardbuddy.domain.enums.SpecializationType;
import com.beardbuddy.domain.enums.UserRole;
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
@Table(name = "user")
public class User {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "firstName", nullable = false)
    private String firstName;

    @Column(name = "lastName", nullable = false)
    private String lastName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "dateOfBirth", nullable = false)
    private String dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "seniorityLevel")
    private SeniorityLevel seniorityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "specializationType")
    private SpecializationType specializationType;

    @Column(name = "experienceYears")
    private Integer experienceYears;

    @Column(name = "hireDate")
    private String hireDate;

    @Column(name = "description")
    private String description;

    @Column(name = "loyaltyPoints")
    private Integer loyaltyPoints;

    @Column(name = "managementAccess")
    private Boolean managementAccess;

    @Column(name = "canMentor")
    private Boolean canMentor;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "certifications")
    private List<String> certifications;

    @Column(name = "maxClientsPerDay")
    private Integer maxClientsPerDay;

    @Column(name = "scissorsMastery")
    private Boolean scissorsMastery;

    @Column(name = "supportsLongHair")
    private Boolean supportsLongHair;

    @Column(name = "trimMastery")
    private Boolean trimMastery;

    @Column(name = "supportsHotTowel")
    private Boolean supportsHotTowel;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "beardCareKnowledge")
    private List<String> beardCareKnowledge;

    @OneToMany(mappedBy = "barber", fetch = FetchType.LAZY)
    private List<BarberService> barberServices = new ArrayList<>();

    protected User() {
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public UserRole getRole() {
        return role;
    }

    public SeniorityLevel getSeniorityLevel() {
        return seniorityLevel;
    }

    public SpecializationType getSpecializationType() {
        return specializationType;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public String getHireDate() {
        return hireDate;
    }

    public String getDescription() {
        return description;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public Boolean getManagementAccess() {
        return managementAccess;
    }

    public Boolean getCanMentor() {
        return canMentor;
    }

    public List<String> getCertifications() {
        return certifications == null ? List.of() : certifications;
    }

    public Integer getMaxClientsPerDay() {
        return maxClientsPerDay;
    }

    public Boolean getScissorsMastery() {
        return scissorsMastery;
    }

    public Boolean getSupportsLongHair() {
        return supportsLongHair;
    }

    public Boolean getTrimMastery() {
        return trimMastery;
    }

    public Boolean getSupportsHotTowel() {
        return supportsHotTowel;
    }

    public List<String> getBeardCareKnowledge() {
        return beardCareKnowledge == null ? List.of() : beardCareKnowledge;
    }

    public List<BarberService> getBarberServices() {
        return barberServices;
    }

    public List<Service> getServices() {
        return barberServices.stream()
                .map(BarberService::getService)
                .toList();
    }
}
