package com.beardbuddy.web.dto;

import com.beardbuddy.domain.User;
import com.beardbuddy.domain.enums.SeniorityLevel;
import com.beardbuddy.domain.enums.SpecializationType;
import com.beardbuddy.domain.enums.UserRole;

import java.util.List;

public record UserDto(
        String id,
        String firstName,
        String lastName,
        String phone,
        String email,
        String dateOfBirth,
        UserRole role,
        SeniorityLevel seniorityLevel,
        SpecializationType specializationType,
        Integer experienceYears,
        String hireDate,
        String description,
        Integer loyaltyPoints,
        boolean managementAccess,
        boolean canMentor,
        List<String> certifications,
        Integer maxClientsPerDay,
        Boolean scissorsMastery,
        Boolean supportsLongHair,
        Boolean trimMastery,
        Boolean supportsHotTowel,
        List<String> beardCareKnowledge
) {

    public static UserDto from(User u) {
        return new UserDto(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getPhone(),
                u.getEmail(),
                u.getDateOfBirth(),
                u.getRole(),
                u.getSeniorityLevel(),
                u.getSpecializationType(),
                u.getExperienceYears(),
                u.getHireDate(),
                u.getDescription(),
                u.getLoyaltyPoints(),
                Boolean.TRUE.equals(u.getManagementAccess()),
                Boolean.TRUE.equals(u.getCanMentor()),
                u.getCertifications(),
                u.getMaxClientsPerDay(),
                u.getScissorsMastery(),
                u.getSupportsLongHair(),
                u.getTrimMastery(),
                u.getSupportsHotTowel(),
                u.getBeardCareKnowledge()
        );
    }
}
