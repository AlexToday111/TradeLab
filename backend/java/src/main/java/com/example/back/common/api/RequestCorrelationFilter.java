package com.example.back.common.api;

import com.example.back.common.logging.LogContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = request.getHeader(LogContext.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = "req-" + UUID.randomUUID();
        }

        request.setAttribute(LogContext.REQUEST_CORRELATION_ID_ATTRIBUTE, correlationId);
        response.setHeader(LogContext.CORRELATION_ID_HEADER, correlationId);

        try (LogContext.BoundContext ignored = LogContext.bind(correlationId, null)) {
            filterChain.doFilter(request, response);
        }
    }
}
