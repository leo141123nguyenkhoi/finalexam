
package com.vti.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpRequestLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();

        long startTime = System.nanoTime();

        MDC.put("requestId", requestId);

        response.setHeader("X-Request-Id", requestId);

        try {

            filterChain.doFilter(request, response);

        } finally {

            long duration = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - startTime
            );

            int status = response.getStatus();

            String method = request.getMethod();

            String path = request.getRequestURI()
                    .replace('\r', '_')
                    .replace('\n', '_');

            if (path.length() > 300) {
                path = path.substring(0, 300);
            }

            if (status >= 400) {

                log.warn(
                        "HTTP {} {} -> {} ({}ms)",
                        method,
                        path,
                        status,
                        duration
                );

            } else {

                log.info(
                        "HTTP {} {} -> {} ({}ms)",
                        method,
                        path,
                        status,
                        duration
                );
            }

            MDC.remove("requestId");
        }
    }
}
