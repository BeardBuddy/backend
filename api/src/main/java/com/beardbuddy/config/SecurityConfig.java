package com.beardbuddy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String[] allowedOrigins;

    public SecurityConfig(@Value("${beardbuddy.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            @Value("${beardbuddy.rate-limit.auth-per-minute}") int authPerMinute,
            @Value("${beardbuddy.rate-limit.api-per-minute}") int apiPerMinute,
            @Value("${beardbuddy.rate-limit.trusted-proxies}") int trustedProxies
    ) throws Exception {
        // After the JWT filter, so authenticated calls are counted per user rather than per IP.
        RateLimitFilter rateLimitFilter = new RateLimitFilter(authPerMinute, apiPerMinute, trustedProxies);

        return http
                // Stateless JWT in an httpOnly cookie: no server session, so no CSRF token store.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** Cost factor comes from BEARDBUDDY_BCRYPT_ROUNDS; existing hashes keep working at any value. */
    @Bean
    public PasswordEncoder passwordEncoder(@Value("${beardbuddy.password.bcrypt-rounds}") int rounds) {
        if (rounds < 4 || rounds > 31) {
            throw new IllegalStateException("BEARDBUDDY_BCRYPT_ROUNDS must be between 4 and 31, got " + rounds);
        }
        return new BCryptPasswordEncoder(rounds);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        // Required for the browser to send and store the access_token cookie.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
