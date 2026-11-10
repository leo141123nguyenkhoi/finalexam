
package com.vti.controller;

import com.vti.dto.AccountDTO;
import com.vti.exception.ApiException;
import com.vti.repository.IAccountRepository;
import com.vti.service.MapperService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
public class HomeController {

    private final IAccountRepository accountRepository;
    private final MapperService mapperService;



    @GetMapping
    @Transactional(readOnly = true)
    public Map<String, Object> home(
            Authentication authentication
    ) {

        String username = authentication.getName();

        var account = accountRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "error.notFound"
                ));

        AccountDTO user = mapperService.account(account);

        return Map.of(
                "message", "Welcome to Final Exam!",
                "user", user
        );
    }
}
