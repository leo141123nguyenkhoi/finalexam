
package com.vti.dto;

import com.vti.entity.DepartmentType;
import java.time.LocalDateTime;

public record DepartmentDTO(
        Long id,
        String name,
        DepartmentType type,
        long totalMember,
        LocalDateTime createdDate
) {
}
