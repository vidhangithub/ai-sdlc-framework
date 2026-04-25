package com.company.accounts.domain.dto;

import lombok.*;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountListResponseDto {

    private String customerId;
    private int totalCount;
    private List<AccountResponseDto> accounts;
}