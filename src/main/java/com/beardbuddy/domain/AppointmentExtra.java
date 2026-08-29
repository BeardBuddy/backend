package com.beardbuddy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "appointment_extra")
@IdClass(AppointmentExtra.Key.class)
public class AppointmentExtra {

    @Id
    @Column(name = "appointmentId")
    private String appointmentId;

    @Id
    @Column(name = "extraServiceId")
    private String extraServiceId;

    protected AppointmentExtra() {
    }

    public AppointmentExtra(String appointmentId, String extraServiceId) {
        this.appointmentId = appointmentId;
        this.extraServiceId = extraServiceId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getExtraServiceId() {
        return extraServiceId;
    }

    public static class Key implements Serializable {

        private String appointmentId;
        private String extraServiceId;

        public Key() {
        }

        public Key(String appointmentId, String extraServiceId) {
            this.appointmentId = appointmentId;
            this.extraServiceId = extraServiceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key other)) {
                return false;
            }
            return Objects.equals(appointmentId, other.appointmentId)
                    && Objects.equals(extraServiceId, other.extraServiceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(appointmentId, extraServiceId);
        }
    }
}
