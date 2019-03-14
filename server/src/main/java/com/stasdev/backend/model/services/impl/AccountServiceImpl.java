package com.stasdev.backend.model.services.impl;

import com.stasdev.backend.errors.NotEnoughAmountOnAccount;
import com.stasdev.backend.errors.NotImplementedYet;
import com.stasdev.backend.errors.ThereIsNoAccountsWithId;
import com.stasdev.backend.errors.UserNotFound;
import com.stasdev.backend.model.entitys.*;
import com.stasdev.backend.model.repos.AccountRepository;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import com.stasdev.backend.model.repos.RoleRepository;
import com.stasdev.backend.model.repos.TransactionRepository;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements com.stasdev.backend.model.services.AccountService {

    private static final String NO_ACCOUNTS_WITH_ID = "User '%s' have no account with id %d";
    private static final String ON_ACCOUNT_NOT_ENOUGH_MONEY = "On account with number %s amount of money is %s and that not enough for transaction with amount %s";
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, ApplicationUserRepository userRepository, RoleRepository roleRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
    @Transactional
    public void deleteAccount(String userName, Long id){
        Role admin = roleRepository.findByRole("admin").orElseThrow(() -> new RuntimeException("Not have role admin"));
        Account one = accountRepository.getOne(id);
        ApplicationUser byUsername = userRepository.findByUsername(userName);
        if (!byUsername.getRoles().contains(admin) && !one.getUser().getUsername().equals(userName)){
            throw new ThereIsNoAccountsWithId(String.format("You don't has account with id %d", id));
        }
        if (byUsername.getRoles().contains(admin)){
            ApplicationUser userAcc = userRepository.findByUsername(one.getUser().getUsername());
            userAcc.getAccounts().remove(one);
            userRepository.saveAndFlush(userAcc);
        }else {
            byUsername.getAccounts().remove(one);
            userRepository.saveAndFlush(byUsername);
        }
        accountRepository.deleteById(id);
        accountRepository.flush();
    }

    @Override
    @Transactional
    public synchronized void transaction(Transaction transaction){
        transaction.setStartDateTime(LocalDateTime.now());
        try {
            ApplicationUser userFrom = userRepository.findByUsername(transaction.getUserFrom());
            if (userFrom == null){
                throw new UserNotFound(String.format("User with name %s not found",transaction.getUserFrom()));
            }
            ApplicationUser userTo = userRepository.findByUsername(transaction.getUserTo());
            if (userTo == null){
                throw new UserNotFound(String.format("User with name %s not found",transaction.getUserTo()));
            }
            Account accountFrom = accountRepository.findAccountsByUser(userFrom)
                    .orElseThrow(() -> new UserNotFound(String.format("User with name %s not found", userFrom.getUsername())))
                    .stream()
                    .filter(a -> a.getId().equals(transaction.getAccountIdFrom()))
                    .findAny()
                    .orElseThrow(() -> new ThereIsNoAccountsWithId(String.format(NO_ACCOUNTS_WITH_ID, userFrom.getUsername(), transaction.getAccountIdFrom())));
            Account accountTo = accountRepository.findAccountsByUser(userTo)
                    .orElseThrow(() -> new UserNotFound(String.format("User with name %s not found", userTo.getUsername())))
                    .stream()
                    .filter(a -> a.getId().equals(transaction.getAccountIdTo()))
                    .findAny()
                    .orElseThrow(() -> new ThereIsNoAccountsWithId(String.format(NO_ACCOUNTS_WITH_ID, userTo.getUsername(), transaction.getAccountIdTo())));
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
