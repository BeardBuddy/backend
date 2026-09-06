package com.beardbuddy.application;

import com.beardbuddy.domain.PromoCode;
import com.beardbuddy.domain.exception.DomainRuleException;
import com.beardbuddy.web.dto.PromoCodeResultDto;
import org.springframework.stereotype.Component;

@Component
public class PromoCodeService {

    public PromoCodeResultDto apply(String code, Double total) {
        double baseTotal = total == null ? 0 : total;

        PromoCode promoCode = PromoCode.findByCode(code)
                .filter(PromoCode::isValid)
                .orElseThrow(() -> new DomainRuleException("Code cannot be applied"));

        double discountAmount = promoCode.calculateDiscount(baseTotal);

        return new PromoCodeResultDto(
                promoCode.getCode(),
                promoCode.getDiscountPercent(),
                discountAmount,
                Math.max(0, Math.round((baseTotal - discountAmount) * 100.0) / 100.0)
        );
    }
}
