package com.beardbuddy.web;

import com.beardbuddy.application.AuthService;
import com.beardbuddy.config.JwtService;
import com.beardbuddy.web.dto.CustomerDto;
import com.beardbuddy.web.dto.LoginRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    // Cross-site by default in the cloud: the browser app and the API sit on different
    // hosts, and a Lax cookie is dropped on those requests. Configurable so a same-origin
    // local run (docker compose, minikube) can stay on Lax over plain http.
    @Value("${beardbuddy.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${beardbuddy.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerDto> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());

        // httpOnly so JavaScript cannot read it. SameSite/Secure come from config because
        // SameSite=None is only honoured on a Secure cookie, and Secure needs https.
        ResponseCookie cookie = ResponseCookie.from(JwtService.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(jwtService.getTtl())
                .sameSite(cookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authService.currentUser(subjectOf(token)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout() {
        // Must mirror the login cookie's attributes or the browser will not overwrite it.
        ResponseCookie cleared = ResponseCookie.from(JwtService.COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleared.toString())
                .body(Map.of("ok", true));
    }

    private String subjectOf(String token) {
        return jwtService.verify(token).orElseThrow().getSubject();
    }
}
