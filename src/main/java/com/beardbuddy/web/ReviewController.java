package com.beardbuddy.web;

import com.beardbuddy.store.ReviewStore;
import com.beardbuddy.web.dto.ReviewCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewStore reviewStore;

    public ReviewController(ReviewStore reviewStore) {
        this.reviewStore = reviewStore;
    }

    @PostMapping("/api/reviews")
    public ResponseEntity<?> create(@RequestBody ReviewCreateRequest request) {
        try {
            reviewStore.create(request);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("[POST /api/reviews]", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save review"));
        }
    }
}
