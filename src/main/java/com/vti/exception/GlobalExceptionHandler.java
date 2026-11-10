
package com.vti.exception;

import jakarta.validation.ConstraintViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public record ApiError(
            Instant timestamp,
            int status,
            String code,
            String message,
            Map<String, String> details
    ) {
    }

    private String translate(String code) {

        Locale locale = LocaleContextHolder.getLocale();

        return messageSource.getMessage(
                code,
                null,
                code,
                locale
        );
    }

    private ApiError error(
            int status,
            String code,
            String message,
            Map<String, String> details
    ) {
        return new ApiError(
                Instant.now(),
                status,
                code,
                message,
                details
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(
            ApiException ex
    ) {

        log.warn(
                "Business error: code={}, status={}",
                ex.getCode(),
                ex.getStatus().value()
        );

        return ResponseEntity.status(ex.getStatus()).body(
                error(
                        ex.getStatus().value(),
                        ex.getCode(),
                        translate(ex.getCode()),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> details =
                new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError -> {

                    details.putIfAbsent(
                            fieldError.getField(),
                            String.valueOf(
                                    fieldError.getDefaultMessage()
                            )
                    );
                });

        log.warn(
                "Validation failed: fields={}",
                details.keySet()
        );

        return ResponseEntity.badRequest().body(
                error(
                        400,
                        "VALIDATION_ERROR",
                        translate("error.validation"),
                        details
                )
        );
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiError> handleMethodValidation(
            Exception ex
    ) {

        log.warn(
                "Method parameter validation failed: {}",
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.badRequest().body(
                error(
                        400,
                        "VALIDATION_ERROR",
                        translate("error.validation"),
                        Map.of()
                )
        );
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(
            Exception ex
    ) {

        log.warn(
                "Invalid request: {}",
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.badRequest().body(
                error(
                        400,
                        "BAD_REQUEST",
                        translate("error.invalidJson"),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            NoResourceFoundException ex
    ) {

        return ResponseEntity.status(
                HttpStatus.NOT_FOUND
        ).body(
                error(
                        404,
                        "NOT_FOUND",
                        translate("error.notFoundUrl"),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex
    ) {

        return ResponseEntity.status(405).body(
                error(
                        405,
                        "METHOD_NOT_ALLOWED",
                        translate("error.badRequest"),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaType(
            HttpMediaTypeNotSupportedException ex
    ) {

        return ResponseEntity.status(415).body(
                error(
                        415,
                        "UNSUPPORTED_MEDIA_TYPE",
                        translate("error.badRequest"),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleForbidden(
            AccessDeniedException ex
    ) {

        log.warn("Access denied");

        return ResponseEntity.status(
                HttpStatus.FORBIDDEN
        ).body(
                error(
                        403,
                        "FORBIDDEN",
                        translate("error.forbidden"),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex
    ) {

        // Ghi day du stack trace vao Application.log
        log.error("Unexpected backend exception", ex);

        return ResponseEntity.internalServerError().body(
                error(
                        500,
                        "INTERNAL_SERVER_ERROR",
                        translate("error.unexpected"),
                        Map.of()
                )
        );
    }
}
