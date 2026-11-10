
package com.vti.service;

import com.vti.dto.DepartmentDTO;
import com.vti.dto.PageResponse;
import com.vti.entity.Account;
import com.vti.entity.Department;
import com.vti.entity.DepartmentType;
import com.vti.exception.ApiException;
import com.vti.form.DepartmentForm;
import com.vti.repository.IAccountRepository;
import com.vti.repository.IDepartmentRepository;
import com.vti.specification.DepartmentSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService implements IDepartmentService {

    private final IDepartmentRepository departmentRepository;
    private final IAccountRepository accountRepository;
    private final MapperService mapperService;

    private Department findDepartment(long id) {

        return departmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "error.notFound"
                ));
    }

    private void checkUnique(String name, Long exceptId) {

        departmentRepository.findByNameIgnoreCase(name)
                .ifPresent(department -> {

                    if (!department.getId().equals(exceptId)) {
                        throw new ApiException(
                                HttpStatus.CONFLICT,
                                "error.duplicateDepartment"
                        );
                    }
                });
    }

    private Sort createSort(String sortBy, String direction) {

        Set<String> allowed = Set.of(
                "totalMember",
                "createdDate",
                "name",
                "type"
        );

        if (sortBy == null || !allowed.contains(sortBy)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.badRequest"
            );
        }

        if (direction == null ||
                (!direction.equalsIgnoreCase("asc")
                        && !direction.equalsIgnoreCase("desc"))) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.badRequest"
            );
        }

        Sort.Direction order =
                Sort.Direction.fromString(direction);

        return Sort.by(order, sortBy)
                .and(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentDTO> list(
            int page,
            String search,
            DepartmentType type,
            LocalDate minDate,
            LocalDate maxDate,
            String sortBy,
            String direction
    ) {

        if (page < 0 ||
                (minDate != null && maxDate != null
                        && minDate.isAfter(maxDate))) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.badRequest"
            );
        }

        var pageable = PageRequest.of(
                page,
                10,
                createSort(sortBy, direction)
        );

        var result = departmentRepository.findAll(
                DepartmentSpecification.filter(
                        search,
                        type,
                        minDate,
                        maxDate
                ),
                pageable
        );

        return PageResponse.from(
                result,
                mapperService::department
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDTO get(long id) {

        return mapperService.department(
                findDepartment(id)
        );
    }

    @Override
    public DepartmentDTO create(DepartmentForm form) {

        String name = form.name().trim();

        checkUnique(name, null);

        Department department = new Department();

        department.setName(name);
        department.setType(form.type());

        Department saved =
                departmentRepository.saveAndFlush(department);

        return mapperService.department(saved);
    }

    @Override
    public DepartmentDTO update(
            long id,
            DepartmentForm form
    ) {

        Department department = findDepartment(id);

        String name = form.name().trim();

        checkUnique(name, id);

        department.setName(name);
        department.setType(form.type());

        return mapperService.department(
                departmentRepository.saveAndFlush(department)
        );
    }

    @Override
    public void delete(long id) {

        Department department = findDepartment(id);

        // Chuyen cac Account ve trang thai chua co phong ban
        accountRepository.detachDepartment(id);

        // Xoa phong ban
        departmentRepository.delete(department);
    }

    @Override
    public void addMembers(
            long departmentId,
            List<Long> accountIds
    ) {

        Department department = findDepartment(departmentId);

        if (accountIds == null
                || accountIds.isEmpty()
                || accountIds.size() > 100
                || accountIds.stream().anyMatch(
                id -> id == null || id <= 0)) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.badRequest"
            );
        }

        List<Long> uniqueIds = accountIds.stream()
                .distinct()
                .toList();

        List<Account> accounts =
                accountRepository.findAllById(uniqueIds);

        if (accounts.size() != uniqueIds.size()) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "error.notFound"
            );
        }

        for (Account account : accounts) {
            account.setDepartment(department);
        }

        accountRepository.saveAllAndFlush(accounts);
    }

    @Override
    public void removeMember(
            long departmentId,
            long accountId
    ) {

        findDepartment(departmentId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "error.notFound"
                ));

        if (account.getDepartment() == null
                || !Objects.equals(
                account.getDepartment().getId(),
                departmentId
        )) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.badRequest"
            );
        }

        account.setDepartment(null);

        accountRepository.save(account);
    }
}
