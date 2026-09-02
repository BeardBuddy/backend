package com.beardbuddy.web;

import com.beardbuddy.application.PromoCodeService;
import com.beardbuddy.web.dto.ApplyPromoCodeRequest;
import com.beardbuddy.web.dto.PromoCodeResultDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promo-codes")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    public PromoCodeController(PromoCodeService promoCodeService) {
        this.promoCodeService = promoCodeService;
    }

    @PostMapping("/apply")
    public PromoCodeResultDto apply(@RequestBody ApplyPromoCodeRequest request) {
        return promoCodeService.apply(request.code(), request.total());
    }
}
