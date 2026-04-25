package com.company.accounts.service;

import com.company.accounts.domain.document.Account;
import com.company.accounts.domain.dto.AccountListResponseDto;
import com.company.accounts.domain.dto.AccountResponseDto;
import com.company.accounts.domain.mapper.AccountMapper;
import com.company.accounts.repository.AccountRepository;
import com.company.accounts.service.impl.AccountQueryServiceImpl;
import com.company.accounts.util.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountQueryService — Unit Tests")
class AccountQueryServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    AccountMapper accountMapper;

    @InjectMocks
    AccountQueryServiceImpl service;

    private static final UUID CUSTOMER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final Pageable PAGEABLE = PageRequest.of(0, 20);

    @Nested
    @DisplayName("getAccounts() — no status filter")
    class GetAccountsNoFilter {

        @Test
        @DisplayName("should return accounts when customer has accounts")
        void getAccounts_customerHasAccounts_shouldReturnList() {
            // ARRANGE
            var account = Account.builder()
                .id("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                .customerId(CUSTOMER_ID.toString())
                .accountNumber("12345678")
                .status("ACTIVE")
                .currentBalance(BigDecimal.valueOf(1500.00))
                .build();
            Page<Account> page = new PageImpl<>(List.of(account));

            var dto = AccountResponseDto.builder()
                .accountId("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                .accountNumber("12345678")
                .build();

            when(accountRepository.findByCustomerId(CUSTOMER_ID.toString(), PAGEABLE)).thenReturn(page);
            when(accountMapper.toDtoList(List.of(account))).thenReturn(List.of(dto));

            // ACT
            AccountListResponseDto result = service.getAccounts(CUSTOMER_ID, null, PAGEABLE);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID.toString());
            assertThat(result.getTotalCount()).isEqualTo(1);
            assertThat(result.getAccounts()).hasSize(1);
            verify(accountRepository).findByCustomerId(CUSTOMER_ID.toString(), PAGEABLE);
            verify(accountRepository, never()).findByCustomerIdAndStatus(any(), any(), any());
        }

        @Test
        @DisplayName("should return empty list when customer has no accounts — 200 + empty array (Q-07)")
        void getAccounts_customerHasNoAccounts_shouldReturnEmptyList() {
            // ARRANGE
            Page<Account> emptyPage = new PageImpl<>(List.of());
            when(accountRepository.findByCustomerId(CUSTOMER_ID.toString(), PAGEABLE)).thenReturn(emptyPage);
            when(accountMapper.toDtoList(List.of())).thenReturn(List.of());

            // ACT
            AccountListResponseDto result = service.getAccounts(CUSTOMER_ID, null, PAGEABLE);

            // ASSERT
            assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID.toString());
            assertThat(result.getTotalCount()).isZero();
            assertThat(result.getAccounts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAccounts() — with status filter")
    class GetAccountsWithFilter {

        @Test
        @DisplayName("should delegate to findByCustomerIdAndStatus when ACTIVE filter provided")
        void getAccounts_withStatusActive_shouldCallFilteredQuery() {
            // ARRANGE
            Page<Account> emptyPage = new PageImpl<>(List.of());
            when(accountRepository.findByCustomerIdAndStatus(CUSTOMER_ID.toString(), "ACTIVE", PAGEABLE))
                .thenReturn(emptyPage);
            when(accountMapper.toDtoList(List.of())).thenReturn(List.of());

            // ACT
            service.getAccounts(CUSTOMER_ID, AccountStatus.ACTIVE, PAGEABLE);

            // ASSERT
            verify(accountRepository).findByCustomerIdAndStatus(CUSTOMER_ID.toString(), "ACTIVE", PAGEABLE);
            verify(accountRepository, never()).findByCustomerId(any(), any());
        }

        @Test
        @DisplayName("should delegate to findByCustomerIdAndStatus when CLOSED filter provided")
        void getAccounts_withStatusClosed_shouldCallFilteredQuery() {
            // ARRANGE
            Page<Account> emptyPage = new PageImpl<>(List.of());
            when(accountRepository.findByCustomerIdAndStatus(CUSTOMER_ID.toString(), "CLOSED", PAGEABLE))
                .thenReturn(emptyPage);
            when(accountMapper.toDtoList(List.of())).thenReturn(List.of());

            // ACT
            service.getAccounts(CUSTOMER_ID, AccountStatus.CLOSED, PAGEABLE);

            // ASSERT
            verify(accountRepository).findByCustomerIdAndStatus(CUSTOMER_ID.toString(), "CLOSED", PAGEABLE);
        }
    }
}