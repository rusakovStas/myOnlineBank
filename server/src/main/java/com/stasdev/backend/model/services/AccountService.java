package com.stasdev.backend.model.services;

import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.Suggestion;
import com.stasdev.backend.model.entitys.Transaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccountService {
    List<Account> getAll();

    List<Account> getMyAccount(String name);

    List<Suggestion> getSuggestions(String currentUser);

    Account createAccount(Account account);

    Account nameAccount(Account account);

    void deleteAccount(String userName, Long id);

    void transaction(Transaction transaction);
}
