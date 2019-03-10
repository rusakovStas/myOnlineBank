package com.stasdev.backend.model.services.impl;

import com.stasdev.backend.errors.NotEnoughAmountOnAccount;
import com.stasdev.backend.errors.NotImplementedYet;
import com.stasdev.backend.errors.ThereIsNoAccountsWithId;
import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.Amount;
import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Transaction;
import com.stasdev.backend.model.repos.AccountRepository;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import com.stasdev.backend.model.repos.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AccountServiceImpl {

    private static final String NO_ACCOUNTS_WITH_ID = "No accounts with id %d";
    private static final String ON_ACCOUNT_NOT_ENOUGH_MONEY = "On account with number %s amount of money is %s and that not enough for transaction with amount %s";
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationUserRepository userRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, ApplicationUserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    List<Account> getAll(){
        return null;
    }

    List<Account> getMyAccount(Authentication authentication){
        String name = authentication.getName();
        ApplicationUser byUsername = userRepository.findByUsername(name);
        return accountRepository
                .findAccountsByUser(byUsername)
                .orElse(Collections.emptyList());
    }

    Account createAccount(Account account){
        throw new NotImplementedYet("Operation 'Create' not implemented yet");
    }

    Account nameAccount(Account account){
        return null;
    }

    void deleteAccount(Long id){

    }

    @Transactional
    void transaction(Transaction transaction){
        transaction.setStartDateTime(LocalDateTime.now());
        try {
            Account accountFrom = accountRepository
                    .findById(transaction.getAccountIdFrom())
                    .orElseThrow(() -> new ThereIsNoAccountsWithId(String.format(NO_ACCOUNTS_WITH_ID, transaction.getAccountIdFrom())));
            Account accountTo = accountRepository
                    .findById(transaction.getAccountIdTo())
                    .orElseThrow(() -> new ThereIsNoAccountsWithId(String.format(NO_ACCOUNTS_WITH_ID, transaction.getAccountIdTo())));
            Amount amount = transaction.getAmount();
            Amount amountFrom = accountFrom.getAmount();
            Amount amountTo = accountTo.getAmount();
            if (amountFrom.compareTo(amount) < 0) {
                throw new NotEnoughAmountOnAccount(String.format(ON_ACCOUNT_NOT_ENOUGH_MONEY, accountFrom.getNumber(), accountFrom.getAmount().getSum(), amount.getSum()));
            }

            BigDecimal amountFromSumAfterTransaction = amountFrom.getSum().subtract(amount.getSum());
            accountFrom
                    .getAmount()
                    .setSum(amountFromSumAfterTransaction);

            BigDecimal amountToSumAfterTransaction = amountTo.getSum().add(amount.getSum());
            accountTo
                    .getAmount()
                    .setSum(amountToSumAfterTransaction);

            accountRepository.saveAndFlush(accountFrom);
            accountRepository.saveAndFlush(accountTo);
        }finally {
            transaction.setEndDateTime(LocalDateTime.now());
            transactionRepository.save(transaction);
        }

    }

}
