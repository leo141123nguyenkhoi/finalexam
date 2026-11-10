
package com.vti.config;

import com.vti.entity.Account;
import com.vti.repository.IAccountRepository;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtEncoder jwtEncoder(
            @Value("${app.jwt.secret}") String secret
    ) {
        validateSecret(secret);

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(
                        secret.getBytes(StandardCharsets.UTF_8)
                )
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.jwt.secret}") String secret
    ) {
        validateSecret(secret);

        var key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        "vti-final-exam"
                )
        );

        return decoder;
    }

    private void validateSecret(String secret) {

        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET must have at least 32 bytes"
            );
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}")
            String origins
    ) {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
                Arrays.stream(origins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
        );

        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        config.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept-Language"
                )
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            IAccountRepository accountRepository,
            RestSecurityErrorHandler errorHandler
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {})

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.OPTIONS, "/**"
                        ).permitAll()

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                "/api/auth/change-password"
                        ).authenticated()

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/accounts/**",
                                "/api/departments/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/home",
                                "/api/home/**"
                        ).authenticated()

                        .anyRequest().authenticated()
                )

                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler)
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(token -> {

                                    Account account = accountRepository
                                            .findByUsernameIgnoreCase(
                                                    token.getSubject()
                                            )
                                            .orElseThrow(() ->
                                                    new BadCredentialsException(
                                                            "Account not found"
                                                    )
                                            );

                                    if (!account.isActive()) {
                                        throw new BadCredentialsException(
                                                "Account inactive"
                                        );
                                    }

                                    String authority =
                                            "ROLE_" + account.getRole().name();

                                    return new JwtAuthenticationToken(
                                            token,
                                            List.of(
                                                    new SimpleGrantedAuthority(
                                                            authority
                                                    )
                                            ),
                                            account.getUsername()
                                    );
                                })
                        )
                );

        return http.build();
    }
}
