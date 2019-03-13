package com.stasdev.backend.model.services.impl;

import com.stasdev.backend.errors.NotEnoughAmountOnAccount;
import com.stasdev.backend.errors.NotImplementedYet;
import com.stasdev.backend.errors.ThereIsNoAccountsWithId;
import com.stasdev.backend.model.entitys.*;
import com.stasdev.backend.model.repos.AccountRepository;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import com.stasdev.backend.model.repos.TransactionRepository;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements com.stasdev.backend.model.services.AccountService {

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

    @Override
    public List<Account> getAll(){
        return accountRepository.findAll();
    }

    @Override
    public List<Account> getMyAccount(String name){
        return accountRepository
                .findAccountsByUser(userRepository.findByUsername(name))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Suggestion> getSuggestions(String currentUser){
        return userRepository
                .findAll()
                .stream()
                .flatMap(u -> u.getAccounts().stream()) //разворачиваем все аккаунты этих юзеров в один стрим
                .map(a -> new Pair<>(currentUser, a))
                .map(this::mapAccountToSuggestion) //создаем из информации об аккаунтах предположения
                .collect(Collectors.toList()); //их и отдаем
    }

    private Suggestion mapAccountToSuggestion(Pair<String, Account> pair){
        return new Suggestion(pair.getValue().getNumber(),
                            isItOwnUserAccount(pair) ?
                                    "My own account"
                                    : pair.getValue().getUser().getUsername(),
                            pair.getValue().getId());
    }

    private boolean isItOwnUserAccount(Pair<String, Account> pair){
        return pair.getKey().equals(pair.getValue().getUser().getUsername());
    }

    @Override
    public Account createAccount(Account account){
        throw new NotImplementedYet("Operation 'Create' not implemented yet");
    }

    @Override
    public Account nameAccount(Account account){
        return null;
    }

    @Override
    public void deleteAccount(Long id){

    }

    @Override
    @Transactional
    public void transaction(Transaction transaction){
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
