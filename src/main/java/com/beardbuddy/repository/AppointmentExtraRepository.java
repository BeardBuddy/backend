package com.beardbuddy.repository;

import com.beardbuddy.domain.AppointmentExtra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentExtraRepository extends JpaRepository<AppointmentExtra, AppointmentExtra.Key> {
}
