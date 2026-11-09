package com.beardbuddy.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fixed one-minute windows, counted in memory.
 *
 * <ul>
 *   <li>login / register: per client IP, so password guessing and signup spam are slowed down;</li>
 *   <li>everything else: per signed-in user, or per IP for anonymous calls.</li>
 * </ul>
 *
 * <p>Counts are per API instance; with N replicas the effective limit is up to N times higher.
 * That is fine here — the barber import, the one expensive call, has its own cooldown in the
 * database. Built in SecurityConfig rather than as a bean, so Spring Boot does not also register
 * it as a plain servlet filter.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000;
    private static final int CLEANUP_EVERY = 1_000;

    private final int authPerMinute;
    private final int apiPerMinute;
    private final int trustedProxies;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong requests = new AtomicLong();

    public RateLimitFilter(int authPerMinute, int apiPerMinute, int trustedProxies) {
        this.authPerMinute = authPerMinute;
        this.apiPerMinute = apiPerMinute;
        this.trustedProxies = trustedProxies;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        if (requests.incrementAndGet() % CLEANUP_EVERY == 0) {
            windows.values().removeIf(window -> now - window.start >= WINDOW_MS);
        }

        String path = request.getRequestURI();
        boolean authCall = path.equals("/api/auth/login") || path.equals("/api/auth/register");
        String key = authCall ? "auth:ip:" + clientIp(request) : "api:" + caller(request);
        int limit = authCall ? authPerMinute : apiPerMinute;

        Window window = windows.compute(key, (k, current) -> {
            if (current == null || now - current.start >= WINDOW_MS) {
                return new Window(now);
            }
            current.count++;
            return current;
        });

        if (window.count > limit) {
            long retryAfter = Math.max(1, (window.start + WINDOW_MS - now + 999) / 1000);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"error\":\"Too many requests, try again in " + retryAfter + "s\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String caller(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "user:" + auth.getName();
        }
        return "ip:" + clientIp(request);
    }

    /**
     * Behind a reverse proxy every request comes from the proxy's address, so the client is read
     * from X-Forwarded-For. Each proxy appends the address it saw, so with N trusted proxies the
     * client is the N-th entry from the right; anything further left can be forged by the client.
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (trustedProxies > 0 && forwarded != null && !forwarded.isBlank()) {
            String[] hops = forwarded.split(",");
            int index = Math.max(0, hops.length - trustedProxies);
            return hops[index].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        private final long start;
        private int count = 1;

        private Window(long start) {
            this.start = start;
        }
    }
}
