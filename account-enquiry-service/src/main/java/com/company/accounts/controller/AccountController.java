package com.company.accounts.controller;

import com.company.accounts.domain.dto.AccountListResponseDto;
import com.company.accounts.service.AccountQueryService;
import com.company.accounts.util.AccountStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AccountController {

    private final AccountQueryService accountQueryService;

    @GetMapping("/{customerId}/accounts")
    @PreAuthorize("hasAuthority('SCOPE_accounts:read')")
    public AccountListResponseDto getAccounts(
            @PathVariable UUID customerId,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/v1/customers/{}/accounts status={}", customerId, status);
        return accountQueryService.getAccounts(customerId, status, PageRequest.of(page, size));
    }
}