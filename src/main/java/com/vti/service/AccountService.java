
package com.vti.service;

import com.vti.dto.AccountDTO;
import com.vti.dto.PageResponse;
import com.vti.entity.Account;
import com.vti.entity.Department;
import com.vti.entity.Role;
import com.vti.exception.ApiException;
import com.vti.form.CreatingAccountForm;
import com.vti.form.UpdatingAccountForm;
import com.vti.repository.IAccountRepository;
import com.vti.repository.IDepartmentRepository;
import com.vti.specification.AccountSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements IAccountService {

    private final IAccountRepository accountRepository;
    private final IDepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final MapperService mapperService;

    private Account findAccount(long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "error.notFound"
                ));
    }

    private Department findDepartment(Long id) {

        if (id == null) {
            return null;
        }

        return departmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "error.notFound"
                ));
    }

    private void checkUnique(
            String username,
            String email,
            Long exceptId
    ) {

        accountRepository.findByUsernameIgnoreCase(username)
                .ifPresent(account -> {
                    if (!account.getId().equals(exceptId)) {
                        throw new ApiException(
                                HttpStatus.CONFLICT,
                                "error.duplicateUsername"
                        );
                    }
                });

        accountRepository.findByEmailIgnoreCase(email)
                .ifPresent(account -> {
                    if (!account.getId().equals(exceptId)) {
                        throw new ApiException(
                                HttpStatus.CONFLICT,
                                "error.duplicateEmail"
                        );
                    }
                });
    }

    private Sort createSort(
            String sortBy,
            String direction
    ) {

        Set<String> allowed = Set.of(
                "createdDate",
                "username",
                "firstName",
                "lastName",
                "role"
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
                .and(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccountDTO> list(
            int page,
            String search,
            Role role,
            Long departmentId,
            String sortBy,
            String direction
    ) {

        if (page < 0) {
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

        var result = accountRepository.findAll(
                AccountSpecification.filter(
                        search,
                        role,
                        departmentId
                ),
                pageable
        );

        return PageResponse.from(
                result,
                mapperService::account
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO get(long id) {
        return mapperService.account(findAccount(id));
    }

    @Override
    public AccountDTO create(CreatingAccountForm form) {

        String username = form.username()
                .trim().toLowerCase(Locale.ROOT);

        String email = form.email()
                .trim().toLowerCase(Locale.ROOT);

        checkUnique(username, email, null);

        Account account = new Account();

        account.setUsername(username);
        account.setFirstName(form.firstName().trim());
        account.setLastName(form.lastName().trim());
        account.setEmail(email);

        account.setPasswordHash(
                passwordEncoder.encode(form.password())
        );

        account.setRole(form.role());

        account.setActive(
                form.active() == null || form.active()
        );

        account.setDepartment(
                findDepartment(form.departmentId())
        );

        Account saved = accountRepository.saveAndFlush(account);

        return mapperService.account(saved);
    }

    @Override
    public AccountDTO update(
            long id,
            UpdatingAccountForm form,
            String actor
    ) {

        Account account = findAccount(id);

        String username = form.username()
                .trim().toLowerCase(Locale.ROOT);

        String email = form.email()
                .trim().toLowerCase(Locale.ROOT);

        checkUnique(username, email, id);

        if (account.getUsername().equalsIgnoreCase(actor)
                && (!form.active() || form.role() != Role.ADMIN)) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.selfChange"
            );
        }

        account.setUsername(username);
        account.setFirstName(form.firstName().trim());
        account.setLastName(form.lastName().trim());
        account.setEmail(email);
        account.setRole(form.role());
        account.setActive(form.active());

        account.setDepartment(
                findDepartment(form.departmentId())
        );

        return mapperService.account(
                accountRepository.saveAndFlush(account)
        );
    }

    @Override
    public void deleteBatch(
            List<Long> ids,
            String actor
    ) {

        if (ids == null || ids.isEmpty()
                || ids.size() > 100
                || ids.stream().anyMatch(
                id -> id == null || id <= 0)) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.badRequest"
            );
        }

        List<Long> uniqueIds = ids.stream()
                .distinct()
                .toList();

        List<Account> deleting =
                accountRepository.findAllById(uniqueIds);

        if (deleting.size() != uniqueIds.size()) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "error.notFound"
            );
        }

        boolean deletingSelf = deleting.stream()
                .anyMatch(account ->
                        account.getUsername()
                                .equalsIgnoreCase(actor));

        if (deletingSelf) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "error.selfChange"
            );
        }

        accountRepository.deleteAllInBatch(deleting);
    }
}
