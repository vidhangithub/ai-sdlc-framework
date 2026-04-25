package com.company.accounts.domain.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "${app.mongodb.collection.accounts:accounts}")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Account {

    @Id
    private String id;

    @Indexed
    private String customerId;

    private String accountNumber;
    private String sortCode;
    private String accountType;
    private String currency;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;

    @Indexed
    private String status;

    private String openingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}