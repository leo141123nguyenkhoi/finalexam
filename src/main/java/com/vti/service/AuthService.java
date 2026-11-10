
package com.vti.service;

import com.vti.dto.AccountDTO;
import com.vti.entity.Account;
import com.vti.entity.PasswordResetToken;
import com.vti.entity.Role;
import com.vti.exception.ApiException;
import com.vti.form.AuthForms;
import com.vti.form.RegistrationForm;
import com.vti.repository.IAccountRepository;
import com.vti.repository.PasswordResetTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final IAccountRepository accountRepository;
    private final PasswordResetTokenRepository resetRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final MapperService mapperService;
    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.dev-log:false}")
    private boolean devLog;

    @Value("${app.frontend.url:http://127.0.0.1:5500}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public record LoginResponse(
            String accessToken,
            String tokenType,
            Instant expiresAt,
            AccountDTO user
    ) {}

    @Transactional(readOnly = true)
    public LoginResponse login(AuthForms.Login form) {

        Account account = accountRepository
                .findByUsernameIgnoreCase(
                        form.username().trim()
                )
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "error.credentials"
                ));

        boolean matched = passwordEncoder.matches(
                form.password(),
                account.getPasswordHash()
        );

        if (!matched) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "error.credentials"
            );
        }

        if (!account.isActive()) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "error.inactive"
            );
        }

        Instant now = Instant.now();

        Duration duration = form.rememberMe()
                ? Duration.ofDays(7)
                : Duration.ofHours(2);

        Instant expiresAt = now.plus(duration);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("vti-final-exam")
                .subject(account.getUsername())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("role", account.getRole().name())
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String accessToken = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresAt,
                mapperService.account(account)
        );
    }

    public void register(RegistrationForm form) {

        String username = form.username()
                .trim().toLowerCase(Locale.ROOT);

        String email = form.email()
                .trim().toLowerCase(Locale.ROOT);

        if (accountRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "error.duplicateUsername"
            );
        }

        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "error.duplicateEmail"
            );
        }

        Account account = new Account();

        account.setUsername(username);
        account.setFirstName(form.firstName().trim());
        account.setLastName(form.lastName().trim());
        account.setEmail(email);

        account.setPasswordHash(
                passwordEncoder.encode(form.password())
        );

        account.setRole(Role.EMPLOYEE);

        account.setActive(false);

        accountRepository.save(account);
    }

    public void forgotPassword(AuthForms.Forgot form) {

        accountRepository
                .findByEmailIgnoreCase(form.email().trim())
                .ifPresent(account -> {

                    byte[] randomBytes = new byte[32];
                    new SecureRandom().nextBytes(randomBytes);

                    String rawToken = Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(randomBytes);

                    PasswordResetToken resetToken =
                            new PasswordResetToken();

                    resetToken.setAccount(account);
                    resetToken.setTokenHash(sha256(rawToken));
                    resetToken.setExpiresAt(
                            LocalDateTime.now().plusMinutes(30)
                    );
                    resetToken.setUsed(false);

                    resetRepository.save(resetToken);

                    String resetLink = frontendUrl
                            + "/#reset?token="
                            + URLEncoder.encode(
                            rawToken,
                            StandardCharsets.UTF_8
                    );

                    if (mailEnabled) {

                        JavaMailSender sender =
                                mailSender.getIfAvailable();

                        if (sender == null) {
                            throw new IllegalStateException(
                                    "SMTP is not configured"
                            );
                        }

                        SimpleMailMessage message =
                                new SimpleMailMessage();

                        message.setTo(account.getEmail());

                        if (!mailFrom.isBlank()) {
                            message.setFrom(mailFrom);
                        }

                        message.setSubject(
                                "Final Exam - Reset Password"
                        );

                        message.setText(
                                "Click the link below within 30 minutes:\n"
                                        + resetLink
                        );

                        sender.send(message);

                    } else if (devLog) {

                        log.warn(
                                "DEV ONLY - Reset password link for {}: {}",
                                account.getUsername(),
                                resetLink
                        );
                    }
                });
    }

    public void resetPassword(AuthForms.Reset form) {

        String tokenHash = sha256(form.token());

        PasswordResetToken resetToken = resetRepository
                .findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "error.invalidToken"
                ));

        if (!LocalDateTime.now().isBefore(
                resetToken.getExpiresAt()
        )) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.invalidToken"
            );
        }

        Account account = resetToken.getAccount();

        account.setPasswordHash(
                passwordEncoder.encode(form.newPassword())
        );

        resetToken.setUsed(true);

        accountRepository.save(account);
        resetRepository.save(resetToken);
    }

    public void changePassword(
            String username,
            AuthForms.Change form
    ) {

        Account account = accountRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "error.notFound"
                ));

        if (!passwordEncoder.matches(
                form.oldPassword(),
                account.getPasswordHash()
        )) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.oldPassword"
            );
        }

        if (passwordEncoder.matches(
                form.newPassword(),
                account.getPasswordHash()
        )) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.badRequest"
            );
        }

        account.setPasswordHash(
                passwordEncoder.encode(form.newPassword())
        );

        accountRepository.save(account);
    }

    private String sha256(String value) {

        try {
            byte[] bytes = MessageDigest
                    .getInstance("SHA-256")
                    .digest(
                            value.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(bytes);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
