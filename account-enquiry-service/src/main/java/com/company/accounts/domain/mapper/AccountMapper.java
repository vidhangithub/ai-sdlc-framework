package com.company.accounts.domain.mapper;

import com.company.accounts.domain.document.Account;
import com.company.accounts.domain.dto.AccountResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AccountMapper {

    @Mapping(source = "id", target = "accountId")
    AccountResponseDto toDto(Account account);

    List<AccountResponseDto> toDtoList(List<Account> accounts);
}