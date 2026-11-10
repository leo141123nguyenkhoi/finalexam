
package com.vti.form;

import jakarta.validation.constraints.*;

public record RegistrationForm(

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9_.]{3,40}$")
        String username,

        @NotBlank
        @Size(max = 80)
        String firstName,

        @NotBlank
        @Size(max = 80)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 190)
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
