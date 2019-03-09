package com.stasdev.backend.model.repos;

import com.stasdev.backend.model.entitys.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account getAccountById(Long id);

}
