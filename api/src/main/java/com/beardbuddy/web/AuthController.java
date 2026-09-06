package com.beardbuddy.web;

import com.beardbuddy.application.AuthService;
import com.beardbuddy.config.JwtService;
import com.beardbuddy.web.dto.CustomerDto;
import com.beardbuddy.web.dto.LoginRequest;
import org.springframework.http.HttpHeaders;
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

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerDto> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());

        // httpOnly so JavaScript cannot read it; SameSite=Lax is enough for a same-site SPA.
        ResponseCookie cookie = ResponseCookie.from(JwtService.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtService.getTtl())
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authService.currentUser(subjectOf(token)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout() {
        ResponseCookie cleared = ResponseCookie.from(JwtService.COOKIE_NAME, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleared.toString())
                .body(Map.of("ok", true));
    }

    private String subjectOf(String token) {
        return jwtService.verify(token).orElseThrow().getSubject();
    }
}
