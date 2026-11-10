
package com.vti.form;

import com.vti.entity.DepartmentType;
import jakarta.validation.constraints.*;

public record DepartmentForm(

        @NotBlank
        @Size(min = 2, max = 100)
        String name,

        @NotNull
        DepartmentType type
) {
}
