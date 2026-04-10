package com.bankingsystem.entity;

import com.bankingsystem.dto.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    private BigDecimal balance;

    private Boolean active;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date lastModifiedAt;
}
