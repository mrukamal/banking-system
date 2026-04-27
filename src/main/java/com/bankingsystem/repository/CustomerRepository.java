package com.bankingsystem.repository;

import com.bankingsystem.entity.AccountEntity;
import com.bankingsystem.entity.CustomerEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    boolean existsByNameAndActiveTrue(String name);
    boolean existsByIdAndActiveTrue(Long id);
    Slice<CustomerEntity> findBy(Pageable pageable);
}
