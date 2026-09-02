package com.beardbuddy.application;

import com.beardbuddy.domain.Service;
import com.beardbuddy.domain.User;
import com.beardbuddy.domain.exception.NotFoundException;
import com.beardbuddy.repository.ExtraServiceRepository;
import com.beardbuddy.repository.ServiceRepository;
import com.beardbuddy.repository.UserRepository;
import com.beardbuddy.web.dto.BarberDto;
import com.beardbuddy.web.dto.CustomerDto;
import com.beardbuddy.web.dto.ExtraServiceDto;
import com.beardbuddy.web.dto.ServiceDto;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CatalogService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ExtraServiceRepository extraServiceRepository;

    public CatalogService(
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            ExtraServiceRepository extraServiceRepository
    ) {
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.extraServiceRepository = extraServiceRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> availableServices() {
        return serviceRepository.findAll().stream()
                .filter(Service::isAvailable)
                .map(ServiceDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BarberDto> barbersOfService(String serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        return service.getBarbers().stream()
                .filter(barber -> !barber.getSchedules().isEmpty())
                .map(BarberDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> servicesOfBarber(String barberId) {
        User barber = userRepository.findById(barberId)
                .orElseThrow(() -> new NotFoundException("Barber not found"));
        if (!barber.isBarber()) {
            throw new NotFoundException("Barber not found");
        }
        return barber.getServices().stream().map(ServiceDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ExtraServiceDto> extraServices() {
        return extraServiceRepository.findAll().stream().map(ExtraServiceDto::from).toList();
    }

    @Transactional(readOnly = true)
    public CustomerDto currentCustomer() {
        return userRepository.findAll().stream()
                .filter(User::isCustomer)
                .findFirst()
                .map(CustomerDto::from)
                .orElseThrow(() -> new NotFoundException("No customer configured"));
    }
}
