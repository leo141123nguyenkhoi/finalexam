
package com.vti.service;

import com.vti.dto.AccountDTO;
import com.vti.dto.DepartmentDTO;
import com.vti.entity.Account;
import com.vti.entity.Department;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapperService {

    private final ModelMapper modelMapper;

    public AccountDTO account(Account account) {

        AccountFields fields = modelMapper.map(
                account,
                AccountFields.class
        );

        Department department = account.getDepartment();

        Long departmentId = department == null
                ? null : department.getId();

        String departmentName = department == null
                ? null : department.getName();

        return new AccountDTO(
                account.getId(),
                fields.getUsername(),
                fields.getFirstName(),
                fields.getLastName(),
                fields.getEmail(),
                account.getRole(),
                account.isActive(),
                departmentId,
                departmentName,
                account.getCreatedDate()
        );
    }

    public DepartmentDTO department(Department d) {

        return new DepartmentDTO(
                d.getId(),
                d.getName(),
                d.getType(),
                d.getTotalMember(),
                d.getCreatedDate()
        );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AccountFields {
        private String username;
        private String firstName;
        private String lastName;
        private String email;
    }
}
