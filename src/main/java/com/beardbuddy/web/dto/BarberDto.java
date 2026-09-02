package com.beardbuddy.web.dto;

import com.beardbuddy.domain.User;
import com.beardbuddy.web.json.CompactDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

public record BarberDto(
        String id,
        String firstName,
        String lastName,
        String fullName,
        String seniorityLevel,
        String specializationType,
        String description,
        Integer experienceYears,
        @JsonSerialize(using = CompactDoubleSerializer.class) Double averageRating,
        boolean expert,
        List<String> specializations
) {

    public static BarberDto from(User barber) {
        List<String> specializations = barber.getBarberServices().stream()
                .map(bs -> bs.getSpecializationType().name())
                .distinct()
                .toList();

        return new BarberDto(
                barber.getId(),
                barber.getFirstName(),
                barber.getLastName(),
                barber.getFullName(),
                barber.getSeniorityLevel() == null ? null : barber.getSeniorityLevel().name(),
                barber.getSpecializationType() == null ? null : barber.getSpecializationType().name(),
                barber.getDescription(),
                barber.getExperienceYears(),
                barber.getAverageRating(),
                barber.isExpert(),
                specializations
        );
    }
}
