
package com.vti.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RestSecurityErrorHandler
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        writeError(
                request,
                response,
                401,
                "UNAUTHORIZED",
                "error.unauthorized"
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {

        writeError(
                request,
                response,
                403,
                "FORBIDDEN",
                "error.forbidden"
        );
    }

    private Locale resolveLanguage(
            HttpServletRequest request
    ) {
        String header = request.getHeader("Accept-Language");

        if (header == null || header.isBlank()) {
            return Locale.forLanguageTag("vi");
        }

        try {
            Locale selected = Locale.lookup(
                    Locale.LanguageRange.parse(header),
                    List.of(
                            Locale.forLanguageTag("vi"),
                            Locale.ENGLISH,
                            Locale.FRENCH
                    )
            );

            return selected == null
                    ? Locale.forLanguageTag("vi")
                    : selected;

        } catch (IllegalArgumentException ex) {
            return Locale.forLanguageTag("vi");
        }
    }

    private void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String code,
            String messageKey
    ) throws IOException {

        Locale locale = resolveLanguage(request);

        String message = messageSource.getMessage(
                messageKey,
                null,
                messageKey,
                locale
        );

        response.setStatus(status);

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", status,
                        "code", code,
                        "message", message,
                        "details", Map.of()
                )
        );
    }
}
