package com.beardbuddy.web;

import com.beardbuddy.store.CatalogReader;
import com.beardbuddy.web.dto.ServiceDto;
import com.beardbuddy.web.dto.ServiceWithBarbersDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class CatalogController {

    private static final Logger log = LoggerFactory.getLogger(CatalogController.class);

    private final CatalogReader catalogReader;

    public CatalogController(CatalogReader catalogReader) {
        this.catalogReader = catalogReader;
    }

    @GetMapping("/api/barbers/{id}/services")
    public ResponseEntity<?> servicesOfBarber(@PathVariable String id) {
        try {
            Optional<List<ServiceDto>> services = catalogReader.servicesOfBarber(id);
            return services.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Barber not found")));
        } catch (Exception e) {
            log.error("[GET /api/barbers/{}/services]", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load barber services"));
        }
    }

    @GetMapping("/api/services")
    public ResponseEntity<?> services() {
        try {
            List<ServiceWithBarbersDto> services = catalogReader.allServicesWithBarbers();
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            log.error("[GET /api/services]", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load services"));
        }
    }
}
