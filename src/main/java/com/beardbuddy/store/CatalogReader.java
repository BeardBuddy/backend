package com.beardbuddy.store;

import com.beardbuddy.domain.Service;
import com.beardbuddy.domain.enums.UserRole;
import com.beardbuddy.repository.ServiceRepository;
import com.beardbuddy.repository.UserRepository;
import com.beardbuddy.web.dto.ServiceDto;
import com.beardbuddy.web.dto.ServiceWithBarbersDto;
import com.beardbuddy.web.dto.UserDto;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class CatalogReader {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    public CatalogReader(UserRepository userRepository, ServiceRepository serviceRepository) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public Optional<List<ServiceDto>> servicesOfBarber(String barberId) {
        return userRepository.findById(barberId)
                .filter(user -> user.getRole() == UserRole.BARBER)
                .map(barber -> barber.getServices().stream()
                        .map(ServiceDto::from)
                        .toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceWithBarbersDto> allServicesWithBarbers() {
        return serviceRepository.findAll().stream()
                .map(this::withBarbers)
                .toList();
    }

    private ServiceWithBarbersDto withBarbers(Service service) {
        List<UserDto> barbers = service.getBarbers().stream()
                .map(UserDto::from)
                .toList();
        return ServiceWithBarbersDto.from(service, barbers);
    }
}
