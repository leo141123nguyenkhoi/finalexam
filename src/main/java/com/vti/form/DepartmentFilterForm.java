
package com.vti.form;

import com.vti.entity.DepartmentType;
import java.time.LocalDate;

public record DepartmentFilterForm(
        String search,
        DepartmentType type,
        LocalDate minDate,
        LocalDate maxDate
) {
}
