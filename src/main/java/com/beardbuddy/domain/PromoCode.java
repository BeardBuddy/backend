package com.beardbuddy.domain;

import com.beardbuddy.domain.enums.PromoCodeStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Promo codes are fixtures, not persisted entities: there is no promo_code table and no admin
 * screen that could create one. The catalogue below is the whole set.
 */
public final class PromoCode {

    private static final List<PromoCode> FIXTURES = List.of(
            new PromoCode("AAAA", 20, PromoCodeStatus.ACTIVE, null),
            new PromoCode("BBBB", 10, PromoCodeStatus.LIMIT_REACHED, null),
            new PromoCode("CCCC", 15, PromoCodeStatus.EXPIRED, LocalDate.of(2026, 1, 31))
    );

    private final String code;
    private final int discountPercent;
    private final PromoCodeStatus status;
    private final LocalDate expiresAt;

    private PromoCode(String code, int discountPercent, PromoCodeStatus status, LocalDate expiresAt) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public static Optional<PromoCode> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase();
        return FIXTURES.stream().filter(promo -> promo.code.equals(normalized)).findFirst();
    }

    public String getCode() {
        return code;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public PromoCodeStatus getStatus() {
        return status;
    }

    public boolean isExpired() {
        if (expiresAt != null && LocalDate.now().isAfter(expiresAt)) {
            return true;
        }
        return status == PromoCodeStatus.EXPIRED;
    }

    public boolean isLimitReached() {
        return status == PromoCodeStatus.LIMIT_REACHED;
    }

    public boolean isValid() {
        return status == PromoCodeStatus.ACTIVE && !isExpired() && !isLimitReached();
    }

    public double calculateDiscount(double total) {
        return Math.round(total * (discountPercent / 100.0) * 100.0) / 100.0;
    }
}
