package com.beardbuddy.web;

import com.beardbuddy.application.AuthService;
import com.beardbuddy.config.JwtService;
import com.beardbuddy.web.dto.CustomerDto;
import com.beardbuddy.web.dto.LoginRequest;
import com.beardbuddy.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final String cookieSameSite;
    private final boolean cookieSecure;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            // Cross-site in the cloud (frontend and API on different hosts) needs SameSite=None,
            // which browsers only honour on a Secure cookie. Same-host local runs stay on Lax.
            @Value("${beardbuddy.cookie.same-site}") String cookieSameSite,
            @Value("${beardbuddy.cookie.secure}") boolean cookieSecure
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.cookieSameSite = cookieSameSite;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerDto> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());
        return withSession(ResponseEntity.ok(), token);
    }

    /** Creates a customer account and signs it in, the same way login does. */
    @PostMapping("/register")
    public ResponseEntity<CustomerDto> register(@RequestBody RegisterRequest request) {
        String token = authService.register(request);
        return withSession(ResponseEntity.status(HttpStatus.CREATED), token);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                .body(Map.of("ok", true));
    }

    private ResponseEntity<CustomerDto> withSession(ResponseEntity.BodyBuilder response, String token) {
        String userId = jwtService.verify(token).orElseThrow().getSubject();
        return response
                .header(HttpHeaders.SET_COOKIE, cookie(token, jwtService.getTtl()).toString())
                .body(authService.currentUser(userId));
    }

    /** httpOnly so JavaScript cannot read it. Logout must use the same attributes to overwrite it. */
    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(JwtService.COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite)
                .build();
    }
}
