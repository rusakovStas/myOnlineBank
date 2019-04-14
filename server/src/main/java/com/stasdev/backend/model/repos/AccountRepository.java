package com.stasdev.backend.model.repos;

import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAccountsByUser(ApplicationUser user);
}
