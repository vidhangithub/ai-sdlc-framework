package com.company.accounts.service;

import com.company.accounts.domain.dto.AccountListResponseDto;
import com.company.accounts.util.AccountStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AccountQueryService {

    AccountListResponseDto getAccounts(UUID customerId, AccountStatus status, Pageable pageable);
}