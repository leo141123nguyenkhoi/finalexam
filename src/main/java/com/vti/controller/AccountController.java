
package com.vti.controller;

import com.vti.dto.AccountDTO;
import com.vti.dto.PageResponse;
import com.vti.entity.Role;
import com.vti.form.CreatingAccountForm;
import com.vti.form.UpdatingAccountForm;
import com.vti.service.IAccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Account", description = "Quan ly tai khoan")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final IAccountService accountService;

    @GetMapping
    @Operation(summary = "Danh sach, tim kiem, loc Account")
    public PageResponse<AccountDTO> getAllAccounts(

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Role role,

            @RequestParam(required = false)
            Long departmentId,

            @RequestParam(defaultValue = "createdDate")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction
    ) {
        return accountService.list(
                page,
                search,
                role,
                departmentId,
                sortBy,
                direction
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lay Account theo ID")
    public EntityModel<AccountDTO> getAccount(
            @PathVariable Long id
    ) {

        AccountDTO account = accountService.get(id);

        return EntityModel.of(
                account,
                Link.of("/api/accounts/" + id).withSelfRel(),
                Link.of("/api/accounts").withRel("collection")
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tao Account moi")
    public AccountDTO createAccount(
            @Valid @RequestBody CreatingAccountForm form
    ) {
        return accountService.create(form);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cap nhat Account")
    public AccountDTO updateAccount(

            @PathVariable Long id,

            @Valid @RequestBody UpdatingAccountForm form,

            Authentication authentication
    ) {
        return accountService.update(
                id,
                form,
                authentication.getName()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Xoa mot Account")
    public void deleteAccount(
            @PathVariable Long id,
            Authentication authentication
    ) {
        accountService.deleteBatch(
                List.of(id),
                authentication.getName()
        );
    }

    @DeleteMapping("/bulk")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Xoa nhieu Account")
    public void deleteMultipleAccounts(
            @RequestBody List<Long> ids,
            Authentication authentication
    ) {
        accountService.deleteBatch(
                ids,
                authentication.getName()
        );
    }
}
