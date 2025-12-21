package com.bankingsystem.repository;

import com.bankingsystem.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    boolean existsByAccountHolderNameAndActiveTrue(String accountHolderName);
}
