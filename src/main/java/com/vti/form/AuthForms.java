
package com.vti.form;

import jakarta.validation.constraints.*;

public final class AuthForms {

    private AuthForms() {
    }

    public record Login(

            @NotBlank
            String username,

            @NotBlank
            String password,

            boolean rememberMe
    ) {
    }

    public record Forgot(

            @NotBlank
            @Email
            String email
    ) {
    }

    public record Reset(

            @NotBlank
            String token,

            @NotBlank
            @Size(min = 8, max = 72)
            String newPassword
    ) {
    }

    public record Change(

            @NotBlank
            String oldPassword,

            @NotBlank
            @Size(min = 8, max = 72)
            String newPassword
    ) {
    }
}
