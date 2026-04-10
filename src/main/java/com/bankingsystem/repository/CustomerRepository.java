package com.bankingsystem.repository;

import com.bankingsystem.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    boolean existsByNameAndActiveTrue(String name);
    boolean existsByEmailAndActiveTrue(String email);
    boolean existsByIdAndActiveTrue(Long id);
}
