package com.beardbuddy.repository;

import com.beardbuddy.domain.BarberService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarberServiceRepository extends JpaRepository<BarberService, String> {
}
