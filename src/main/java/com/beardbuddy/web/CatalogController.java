package com.beardbuddy.web;

import com.beardbuddy.application.CatalogService;
import com.beardbuddy.web.dto.BarberDto;
import com.beardbuddy.web.dto.CustomerDto;
import com.beardbuddy.web.dto.ExtraServiceDto;
import com.beardbuddy.web.dto.ServiceDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/services")
    public List<ServiceDto> services() {
        return catalogService.availableServices();
    }

    @GetMapping("/services/{id}/barbers")
    public List<BarberDto> barbersOfService(@PathVariable String id) {
        return catalogService.barbersOfService(id);
    }


    @GetMapping("/extra-services")
    public List<ExtraServiceDto> extraServices() {
        return catalogService.extraServices();
    }

    @GetMapping("/customers/current")
    public CustomerDto currentCustomer() {
        return catalogService.currentCustomer();
    }
}
