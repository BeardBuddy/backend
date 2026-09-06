package com.beardbuddy.domain;

import com.beardbuddy.domain.exception.DomainRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @Column(name = "id")
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "date", nullable = false)
    private String date;

    protected Review() {
    }

    Review(String id, Appointment appointment, User customer, int rating, String comment, LocalDate date) {
        if (rating < 1 || rating > 5) {
            throw new DomainRuleException("Rating must be between 1 and 5");
        }
        this.id = id;
        this.appointment = appointment;
        this.customer = customer;
        this.rating = rating;
        this.comment = comment;
        this.date = (date == null ? LocalDate.now() : date).toString();
    }

    public String getId() {
        return id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public User getCustomer() {
        return customer;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }
}
