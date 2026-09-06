package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.AppointmentStatus;
import com.beardbuddy.domain.enums.PaymentMethod;
import com.beardbuddy.domain.enums.PaymentStatus;
import com.beardbuddy.domain.exception.DomainRuleException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barber_id", nullable = false)
    private User barber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(nullable = false)
    private String startTime;

    @Column(nullable = false)
    private String endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Double totalPrice;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "notes")
    private List<String> notes = new ArrayList<>();

    @Column
    private String cancellationReason;

    @Column
    private String paidAt;

    @Column
    private String cancelledAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "appointment_extra",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "extra_service_id")
    )
    private List<ExtraService> extraServices = new ArrayList<>();

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Review review;

    protected Appointment() {
    }

    Appointment(String id, User customer, User barber, Service service, LocalDate date, LocalTime startTime) {
        this.id = id;
        this.customer = customer;
        this.barber = barber;
        this.service = service;
        this.date = date.toString();
        this.startTime = TimeSupport.format(startTime);
        this.endTime = TimeSupport.format(startTime.plusMinutes(service.estimateDuration()));
        this.status = AppointmentStatus.NEW;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.paymentMethod = PaymentMethod.CASH;
        this.totalPrice = service.getPrice();
    }

    public String getId() {
        return id;
    }

    public User getCustomer() {
        return customer;
    }

    public User getBarber() {
        return barber;
    }

    public Service getService() {
        return service;
    }

    public String getDate() {
        return date;
    }

    public LocalDate getDateValue() {
        return LocalDate.parse(date);
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public List<String> getNotes() {
        return notes == null ? List.of() : notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public void setPaymentStatus(PaymentStatus status, PaymentMethod method, double amount) {
        this.paymentStatus = status;
        this.paymentMethod = method;
        this.totalPrice = round(amount);
        this.paidAt = status == PaymentStatus.PAID ? LocalDate.now().toString() : null;
    }

    public void removeExtraService(ExtraService extraService) {
        if (extraServices.removeIf(existing -> existing.getId().equals(extraService.getId()))) {
            recalculateTotal();
        }
    }

    public List<ExtraService> getExtraServices() {
        return extraServices;
    }

    public Review getReview() {
        return review;
    }

    public void addNote(String note) {
        if (note != null && !note.isBlank()) {
            notes.add(note.trim());
        }
    }

    public void addExtraService(ExtraService extraService) {
        boolean alreadyAdded = extraServices.stream()
                .anyMatch(existing -> existing.getId().equals(extraService.getId()));
        if (alreadyAdded) {
            return;
        }
        extraServices.add(extraService);
        recalculateTotal();
    }

    public void applyDiscount(double amount) {
        totalPrice = Math.max(0, round(totalPrice - amount));
    }

    private void recalculateTotal() {
        double extrasTotal = extraServices.stream()
                .mapToDouble(ExtraService::getPrice)
                .sum();
        totalPrice = round(service.getPrice() + extrasTotal);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        LocalTime thisStart = LocalTime.parse(startTime);
        LocalTime thisEnd = LocalTime.parse(endTime);
        return thisStart.isBefore(otherEnd) && thisEnd.isAfter(otherStart);
    }

    public boolean isActive() {
        return status != AppointmentStatus.CANCELLED;
    }

    public boolean isUpcoming() {
        return status == AppointmentStatus.NEW
                || status == AppointmentStatus.CONFIRMED
                || status == AppointmentStatus.IN_PROGRESS;
    }

    public boolean isPast() {
        return status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELLED;
    }

    public boolean canBeReviewed() {
        return status == AppointmentStatus.COMPLETED && review == null;
    }

    public void confirm() {
        this.status = AppointmentStatus.CONFIRMED;
    }

    public void cancel(String reason) {
        if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.COMPLETED) {
            throw new DomainRuleException("Cannot cancel an appointment that is already completed or cancelled");
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDate.now().toString();
    }

    public void complete() {
        if (status != AppointmentStatus.NEW
                && status != AppointmentStatus.CONFIRMED
                && status != AppointmentStatus.IN_PROGRESS) {
            throw new DomainRuleException("Appointment cannot be completed from its current status");
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    public void changeStatus(AppointmentStatus target) {
        switch (target) {
            case CANCELLED -> cancel(cancellationReason);
            case COMPLETED -> complete();
            default -> this.status = target;
        }
    }

    public Review addReview(int rating, String comment, LocalDate reviewDate) {
        if (status != AppointmentStatus.COMPLETED) {
            throw new DomainRuleException("Reviews can only be submitted for completed appointments");
        }
        if (review != null) {
            throw new DomainRuleException("Appointment already has a review");
        }
        Review created = new Review("rev-" + id, this, customer, rating, comment, reviewDate);
        this.review = created;
        return created;
    }
}
