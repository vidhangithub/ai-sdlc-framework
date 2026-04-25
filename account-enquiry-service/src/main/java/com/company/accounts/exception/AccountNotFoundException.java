package com.company.accounts.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID customerId) {
        super(String.format("No accounts found for customerId: %s", customerId));
    }
}