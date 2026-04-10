package com.bankingsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class AccountDTO {
    private Long id;
    private Long customerId;
    private AccountType accountType;
    private BigDecimal balance;
    private Boolean active;
    private Date createdAt;
    private Date lastModifiedAt;
}