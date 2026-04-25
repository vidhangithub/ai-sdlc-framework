package com.company.accounts.repository;

import com.company.accounts.domain.document.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    Page<Account> findByCustomerId(String customerId, Pageable pageable);

    Page<Account> findByCustomerIdAndStatus(String customerId, String status, Pageable pageable);
}