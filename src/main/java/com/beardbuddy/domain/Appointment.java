package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.AppointmentStatus;
import com.beardbuddy.domain.enums.PaymentMethod;
import com.beardbuddy.domain.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "customerId", nullable = false)
    private String customerId;

    @Column(name = "barberId", nullable = false)
    private String barberId;

    @Column(name = "serviceId", nullable = false)
    private String serviceId;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "startTime", nullable = false)
    private String startTime;

    @Column(name = "endTime", nullable = false)
    private String endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentStatus", nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentMethod", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "totalPrice", nullable = false)
    private Double totalPrice;

    @Column(name = "notes")
    private String notes;

    @Column(name = "cancellationReason")
    private String cancellationReason;

    protected Appointment() {
    }

    public Appointment(
            String id,
            String customerId,
            String barberId,
            String serviceId,
            String date,
            String startTime,
            String endTime,
            AppointmentStatus status,
            PaymentStatus paymentStatus,
            PaymentMethod paymentMethod,
            Double totalPrice,
            String notes
    ) {
        this.id = id;
        this.customerId = customerId;
        this.barberId = barberId;
        this.serviceId = serviceId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getBarberId() {
        return barberId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getDate() {
        return date;
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

    public String getNotes() {
        return notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
