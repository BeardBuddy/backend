package com.beardbuddy.store;

import com.beardbuddy.domain.Review;
import com.beardbuddy.web.dto.ReviewCreateRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReviewStore {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void create(ReviewCreateRequest req) {
        entityManager.persist(new Review(
                req.id(),
                req.appointmentId(),
                req.customerId(),
                req.rating(),
                req.comment(),
                req.date()
        ));
        entityManager.flush();
    }
}
