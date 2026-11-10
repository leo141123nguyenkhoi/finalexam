
package com.vti.service;

import com.vti.dto.DepartmentDTO;
import com.vti.dto.PageResponse;
import com.vti.entity.DepartmentType;
import com.vti.form.DepartmentForm;

import java.time.LocalDate;
import java.util.List;

public interface IDepartmentService {

    PageResponse<DepartmentDTO> list(
            int page,
            String search,
            DepartmentType type,
            LocalDate minDate,
            LocalDate maxDate,
            String sortBy,
            String direction
    );

    DepartmentDTO get(long id);

    DepartmentDTO create(DepartmentForm form);

    DepartmentDTO update(long id, DepartmentForm form);

    void delete(long id);

    void addMembers(
            long departmentId,
            List<Long> accountIds
    );

    void removeMember(
            long departmentId,
            long accountId
    );
}
