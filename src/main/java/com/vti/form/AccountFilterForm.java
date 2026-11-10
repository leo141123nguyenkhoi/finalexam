
package com.vti.form;

import com.vti.entity.Role;

public record AccountFilterForm(
        String search,
        Role role,
        Long departmentId
) {
}
