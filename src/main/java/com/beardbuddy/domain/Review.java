package com.beardbuddy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "appointmentId", nullable = false, unique = true)
    private String appointmentId;

    @Column(name = "customerId", nullable = false)
    private String customerId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "date", nullable = false)
    private String date;

    protected Review() {
    }

    public Review(String id, String appointmentId, String customerId, Integer rating, String comment, String date) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getCustomerId() {
        return customerId;
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
