package com.company.accounts.domain.dto;

import lombok.*;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountResponseDto {

    private String accountId;
    private String accountNumber;
    private String sortCode;
    private String accountType;
    private String currency;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private String status;
    private String openingDate;
}