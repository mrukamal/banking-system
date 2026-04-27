package com.bankingsystem.repository;

import com.bankingsystem.dto.AccountType;
import com.bankingsystem.entity.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    boolean existsAccountEntitiesByCustomerIdAndActiveTrueAndAccountType(Long customerId, AccountType accountType);
    Slice<AccountEntity> findBy(Pageable pageable);
}
