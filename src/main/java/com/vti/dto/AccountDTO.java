
package com.vti.dto;

import com.vti.entity.Role;
import java.time.LocalDateTime;

public record AccountDTO(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        Role role,
        boolean active,
        Long departmentId,
        String departmentName,
        LocalDateTime createdDate
) {
}
