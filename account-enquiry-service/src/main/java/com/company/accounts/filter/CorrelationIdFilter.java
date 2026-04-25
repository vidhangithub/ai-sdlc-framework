package com.company.accounts.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(1)
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String correlationId = Optional
            .ofNullable(request.getHeader(CORRELATION_HEADER))
            .filter(h -> !h.isBlank())
            .orElse(UUID.randomUUID().toString());

        MDC.put("traceId", correlationId);
        MDC.put("customerId", extractCustomerId(request));
        response.setHeader(CORRELATION_HEADER, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String extractCustomerId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("customers".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return "unknown";
    }
}