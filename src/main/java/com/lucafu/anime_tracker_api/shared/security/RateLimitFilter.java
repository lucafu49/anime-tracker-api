package com.lucafu.anime_tracker_api.shared.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Límite general: 60 requests por minuto por IP
    private static final int GENERAL_CAPACITY = 60;
    private static final Duration GENERAL_REFILL_PERIOD = Duration.ofMinutes(1);

    // Límite estricto para /auth/login: 5 requests por minuto por IP
    private static final int AUTH_CAPACITY = 5;
    private static final Duration AUTH_REFILL_PERIOD = Duration.ofMinutes(1);

    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String ip = resolveClientIp(request);
        boolean isAuthEndpoint = request.getRequestURI().startsWith("/auth/");

        Bucket bucket = isAuthEndpoint
                ? authBuckets.computeIfAbsent(ip, k -> buildBucket(AUTH_CAPACITY, AUTH_REFILL_PERIOD))
                : generalBuckets.computeIfAbsent(ip, k -> buildBucket(GENERAL_CAPACITY, GENERAL_REFILL_PERIOD));

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            sendRateLimitResponse(response, isAuthEndpoint);
        }
    }

    private Bucket buildBucket(int capacity, Duration period) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, period)
                        .build())
                .build();
    }

    private void sendRateLimitResponse(HttpServletResponse response, boolean isAuthEndpoint) throws IOException {
        String message = isAuthEndpoint
                ? "Too many login attempts. Try again in a minute."
                : "Too many requests. Try again in a minute.";

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write("""
                {"status":429,"error":"Too Many Requests","message":"%s"}
                """.formatted(message));
    }

    /**
     * Extrae la IP real del cliente. Tiene en cuenta el header X-Forwarded-For
     * que añaden los proxies/load balancers (Nginx, Cloudflare, etc.).
     * En caso de múltiples IPs en el header, toma la primera (la del cliente original).
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
