package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.AppointmentStatus;
import com.beardbuddy.domain.enums.SeniorityLevel;
import com.beardbuddy.domain.enums.SpecializationType;
import com.beardbuddy.domain.enums.UserRole;
import com.beardbuddy.domain.exception.DomainRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @Column(name = "id")
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(nullable = false)
    private String dateOfBirth;

    @Column(name = "username", unique = true)
    private String username;

    /** BCrypt hash; null for barbers, who never sign in. */
    @Column
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column
    private SeniorityLevel seniorityLevel;

    @Enumerated(EnumType.STRING)
    @Column
    private SpecializationType specializationType;

    @Column
    private Integer experienceYears;

    @Column
    private String hireDate;

    @Column(name = "description")
    private String description;

    @Column
    private Integer loyaltyPoints;

    @Column
    private Boolean managementAccess;

    @Column
    private Boolean canMentor;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "certifications")
    private List<String> certifications;

    @Column
    private Integer maxClientsPerDay;

    @Column
    private Boolean scissorsMastery;

    @Column
    private Boolean supportsLongHair;

    @Column
    private Boolean trimMastery;

    @Column
    private Boolean supportsHotTowel;

    @Convert(converter = StringListJsonConverter.class)
    @Column
    private List<String> beardCareKnowledge;

    @OneToMany(mappedBy = "barber", fetch = FetchType.LAZY)
    private List<BarberService> barberServices = new ArrayList<>();

    @OneToMany(mappedBy = "barber", fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Appointment> bookedAppointments = new ArrayList<>();

    @OneToMany(mappedBy = "barber", fetch = FetchType.LAZY)
    private List<Appointment> performedAppointments = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Review> writtenReviews = new ArrayList<>();

    protected User() {
    }

    /** Creates a barber. Used by the worker when consuming a BarberCreatedEvent. */
    public static User barber(
            String id,
            String firstName,
            String lastName,
            String phone,
            String dateOfBirth,
            SeniorityLevel seniorityLevel,
            SpecializationType specializationType,
            Integer experienceYears
    ) {
        User user = new User();
        user.id = id;
        user.firstName = firstName;
        user.lastName = lastName;
        user.phone = phone;
        user.dateOfBirth = dateOfBirth;
        user.role = UserRole.BARBER;
        user.seniorityLevel = seniorityLevel;
        user.specializationType = specializationType;
        user.experienceYears = experienceYears;
        user.loyaltyPoints = 0;
        user.managementAccess = false;
        user.canMentor = seniorityLevel == SeniorityLevel.SENIOR;
        return user;
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

    public String getFullName() {
        return firstName + " " + lastName;
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

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isBarber() {
        return role == UserRole.BARBER;
    }

    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
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

    public boolean hasManagementAccess() {
        return Boolean.TRUE.equals(managementAccess);
    }

    public boolean canMentor() {
        return Boolean.TRUE.equals(canMentor);
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

    public List<Schedule> getSchedules() {
        if (isCustomer()) {
            throw new DomainRuleException("Only barbers have schedules");
        }
        return schedules;
    }

    public List<Appointment> getBookedAppointments() {
        return bookedAppointments;
    }

    public List<Appointment> getPerformedAppointments() {
        return performedAppointments;
    }

    public List<Review> getWrittenReviews() {
        return writtenReviews;
    }

    public List<Appointment> getUpcomingAppointments() {
        requireCustomer("Only customers have upcoming appointments");
        return bookedAppointments.stream().filter(Appointment::isUpcoming).toList();
    }

    public List<Appointment> getPastAppointments() {
        requireCustomer("Only customers have past appointments");
        return bookedAppointments.stream().filter(Appointment::isPast).toList();
    }

    public boolean providesService(Service service) {
        return barberServices.stream()
                .anyMatch(bs -> bs.getService().getId().equals(service.getId()));
    }

    public boolean isExpert() {
        if (seniorityLevel != SeniorityLevel.SENIOR) {
            return false;
        }
        boolean hasHaircut = barberServices.stream()
                .anyMatch(bs -> bs.getSpecializationType() == SpecializationType.HAIRCUT);
        boolean hasBeard = barberServices.stream()
                .anyMatch(bs -> bs.getSpecializationType() == SpecializationType.BEARD);
        return hasHaircut && hasBeard;
    }

    public double getAverageRating() {
        if (!isBarber()) {
            throw new DomainRuleException("Only barbers have ratings");
        }
        List<Integer> ratings = performedAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .map(Appointment::getReview)
                .filter(r -> r != null)
                .map(Review::getRating)
                .toList();
        if (ratings.isEmpty()) {
            return 0;
        }
        double sum = ratings.stream().mapToInt(Integer::intValue).sum();
        return Math.round((sum / ratings.size()) * 10.0) / 10.0;
    }

    public boolean isWithinSchedule(LocalDate date, LocalTime time) {
        return schedules.stream().anyMatch(s -> s.covers(date, time));
    }

    public List<Appointment> getAppointmentsForDay(LocalDate date) {
        String dateStr = date.toString();
        List<Appointment> source = isCustomer() ? bookedAppointments : performedAppointments;
        return source.stream().filter(a -> a.getDate().equals(dateStr)).toList();
    }

    /** True when this barber has no conflicting appointment in the given window. */
    public boolean isAvailableAt(LocalDate date, LocalTime start, LocalTime end) {
        return hasNoConflict(performedAppointments, date, start, end);
    }

    /** True when this customer is not already booked elsewhere in the given window. */
    public boolean isFreeAt(LocalDate date, LocalTime start, LocalTime end) {
        return hasNoConflict(bookedAppointments, date, start, end);
    }

    private static boolean hasNoConflict(
            List<Appointment> appointments,
            LocalDate date,
            LocalTime start,
            LocalTime end
    ) {
        return appointments.stream()
                .filter(Appointment::isActive)
                .filter(appointment -> appointment.getDate().equals(date.toString()))
                .noneMatch(appointment -> appointment.overlaps(start, end));
    }

    public Appointment bookAppointment(String appointmentId, User barber, Service service, LocalDate date, LocalTime startTime) {
        requireCustomer("Only customers can book appointments");
        if (!barber.isBarber()) {
            throw new DomainRuleException("Executor must be a barber");
        }
        if (!barber.providesService(service)) {
            throw new DomainRuleException("Selected barber does not provide this service");
        }
        if (barber.schedules.isEmpty()) {
            throw new DomainRuleException("Selected barber has no schedule configured");
        }
        if (!barber.isWithinSchedule(date, startTime)) {
            throw new DomainRuleException("Selected time is outside the barber's working hours");
        }

        LocalTime endTime = startTime.plusMinutes(service.estimateDuration());
        if (!barber.isAvailableAt(date, startTime, endTime)) {
            throw new DomainRuleException("Selected barber is already booked for this time slot");
        }
        if (!isFreeAt(date, startTime, endTime)) {
            throw new DomainRuleException("You already have another appointment at this time");
        }

        Appointment appointment = new Appointment(appointmentId, this, barber, service, date, startTime);
        bookedAppointments.add(appointment);
        barber.performedAppointments.add(appointment);
        return appointment;
    }

    public void confirmAppointment(Appointment appointment) {
        if (!isBarber()) {
            throw new DomainRuleException("Only barbers can approve appointments");
        }
        appointment.confirm();
    }

    public void cancelAppointment(Appointment appointment, String reason) {
        requireCustomer("Only customers can cancel appointments");
        requireOwnership(appointment);
        appointment.cancel(reason);
    }

    public void completeOwnAppointment(Appointment appointment) {
        requireCustomer("Only customers can complete their appointments");
        requireOwnership(appointment);
        appointment.complete();
    }

    public Review submitReview(Appointment appointment, int rating, String comment, LocalDate date) {
        if (isBarber()) {
            throw new DomainRuleException("Barbers cannot write reviews");
        }
        requireCustomer("Only customers can submit reviews");
        requireOwnership(appointment);
        return appointment.addReview(rating, comment, date);
    }

    private void requireCustomer(String message) {
        if (!isCustomer()) {
            throw new DomainRuleException(message);
        }
    }

    private void requireOwnership(Appointment appointment) {
        if (!appointment.getCustomer().getId().equals(id)) {
            throw new DomainRuleException("Appointment does not belong to this customer");
        }
    }
}
