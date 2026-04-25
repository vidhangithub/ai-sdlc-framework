package com.company.accounts.service.impl;

import com.company.accounts.domain.document.Account;
import com.company.accounts.domain.dto.AccountListResponseDto;
import com.company.accounts.domain.dto.AccountResponseDto;
import com.company.accounts.domain.mapper.AccountMapper;
import com.company.accounts.repository.AccountRepository;
import com.company.accounts.service.AccountQueryService;
import com.company.accounts.util.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountQueryServiceImpl implements AccountQueryService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public AccountListResponseDto getAccounts(UUID customerId, AccountStatus status, Pageable pageable) {
        log.info("Querying accounts: customerId={} status={}", customerId, status);

        Page<Account> page = (status == null)
            ? accountRepository.findByCustomerId(customerId.toString(), pageable)
            : accountRepository.findByCustomerIdAndStatus(
                customerId.toString(), status.name(), pageable);

        List<AccountResponseDto> dtos = accountMapper.toDtoList(page.getContent());
        log.info("Found {} accounts for customerId={}", dtos.size(), customerId);

        return AccountListResponseDto.builder()
            .customerId(customerId.toString())
            .totalCount((int) page.getTotalElements())
            .accounts(dtos)
            .build();
    }
}