
package com.vti.controller;

import com.vti.form.AuthForms;
import com.vti.form.RegistrationForm;
import com.vti.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, Register, Password")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Dang nhap va nhan JWT")
    public AuthService.LoginResponse login(
            @Valid @RequestBody AuthForms.Login form
    ) {
        return authService.login(form);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Dang ky tai khoan")
    public Map<String, String> register(
            @Valid @RequestBody RegistrationForm form
    ) {

        authService.register(form);

        return Map.of(
                "message",
                "Registered. Waiting for Admin activation."
        );
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Yeu cau khoi phuc mat khau")
    public Map<String, String> forgotPassword(
            @Valid @RequestBody AuthForms.Forgot form
    ) {

        authService.forgotPassword(form);

        return Map.of(
                "message",
                "If the email exists, reset instructions will be sent."
        );
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Dat lai mat khau bang token")
    public Map<String, String> resetPassword(
            @Valid @RequestBody AuthForms.Reset form
    ) {

        authService.resetPassword(form);

        return Map.of(
                "message",
                "Password reset successfully."
        );
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Doi mat khau khi da dang nhap")
    public Map<String, String> changePassword(

            @Valid @RequestBody AuthForms.Change form,

            Authentication authentication
    ) {

        authService.changePassword(
                authentication.getName(),
                form
        );

        return Map.of(
                "message",
                "Password changed successfully."
        );
    }
}
