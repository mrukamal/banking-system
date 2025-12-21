package com.bankingsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDTO {
    private Long id;
    private String accountHolderName;
    private String accountType; // e.g., SAVINGS, CHECKING
    private BigDecimal balance;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}