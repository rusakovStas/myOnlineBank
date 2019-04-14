package com.stasdev.backend.model.services.impl;

import com.stasdev.backend.errors.*;
import com.stasdev.backend.model.entitys.*;
import com.stasdev.backend.model.repos.AccountRepository;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import com.stasdev.backend.model.repos.RoleRepository;
import com.stasdev.backend.model.repos.TransactionRepository;
import com.stasdev.backend.model.services.AccountService;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private static final String NO_ACCOUNTS_WITH_ID = "User '%s' have no account with id %d";
    private static final String ON_ACCOUNT_NOT_ENOUGH_MONEY = "On account with number %s amount of money is %s and that not enough for transaction with amount %s";
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SocketServiceImpl socketService;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, ApplicationUserRepository userRepository, RoleRepository roleRepository, SocketServiceImpl socketService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.socketService = socketService;
    }

    @Override
    public List<Account> getAll(){
        return accountRepository.findAll();
    }

    @Override
    public List<Account> getMyAccount(String name){
        return accountRepository
                .findAccountsByUser(userRepository.findByUsername(name));
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
        Account one = accountRepository.getOne(id);
        ApplicationUser byUsername = userRepository.findByUsername(userName);
        if (!one.getUser().getUsername().equals(userName)){
            throw new ThereIsNoAccountsWithId(String.format("You don't has account with id %d", id));
        }
        byUsername.getAccounts().remove(one);
        userRepository.saveAndFlush(byUsername);
        accountRepository.deleteById(id);
        accountRepository.flush();
    }

    @Override
    @Transactional
    public synchronized void transaction(Transaction transaction,final String userName){
        transaction.setStartDateTime(LocalDateTime.now());
        Role admin = roleRepository.findByRole("admin").orElseThrow(() -> new RuntimeException("Not have role admin"));
        try {

            ApplicationUser userFrom = getApplicationUserWithCheck(transaction.getUserFrom());
            ApplicationUser userTo = getApplicationUserWithCheck(transaction.getUserTo());
            ApplicationUser currentUser = userRepository.findByUsername(userName);

            boolean currentUserHasRoleAdmin = currentUser.getRoles().contains(admin);

            if (!userFrom.equals(currentUser) && !currentUserHasRoleAdmin){
                throw new UserCanNotDoThisOperation(String.format("User '%s' hasn't role admin and can't do transaction from not his accounts", userFrom.getUsername()));
            }

            Account accountFrom = getAccountFromUserWithCheck(userFrom, transaction.getAccountIdFrom());
            Account accountTo = getAccountFromUserWithCheck(userTo, transaction.getAccountIdTo());

            boolean accountFromBelongsToCurrentUser = currentUser.getAccounts().contains(accountFrom);

            Amount amount = transaction.getAmount();
            Amount amountFrom = accountFrom.getAmount();
            Amount amountTo = accountTo.getAmount();
            if (!currentUserHasRoleAdmin && amountFrom.compareTo(amount) < 0) {
                throw new NotEnoughAmountOnAccount(String.format(ON_ACCOUNT_NOT_ENOUGH_MONEY, accountFrom.getNumber(), accountFrom.getAmount().getSum(), amount.getSum()));
            }
            //Если админ делает транзакцию со своего счета - значение на ней не уменьшаем
            if (!(currentUserHasRoleAdmin && accountFromBelongsToCurrentUser)) {
                BigDecimal amountFromSumAfterTransaction = amountFrom.getSum().subtract(amount.getSum());
                accountFrom
                        .getAmount()
                        .setSum(amountFromSumAfterTransaction);
            }

            BigDecimal amountToSumAfterTransaction = amountTo.getSum().add(amount.getSum());
            accountTo
                    .getAmount()
                    .setSum(amountToSumAfterTransaction);

            socketService.sendPushAboutTransaction(transaction, userName);
            socketService.sendPushWithUpdatedAccounts(accountRepository.saveAndFlush(accountFrom), accountRepository.saveAndFlush(accountTo));
        }finally {
            transaction.setEndDateTime(LocalDateTime.now());
            transactionRepository.save(transaction);
        }

    }

    ApplicationUser getApplicationUserWithCheck(String user) {
        return userRepository
                .getApplicationUserByUsername(user)
                .orElseThrow(() -> new UserNotFound(String.format("User with name %s not found", user)));
    }

    Account getAccountFromUserWithCheck(ApplicationUser userTo, Long accountIdTo) {
        return accountRepository.findAccountsByUser(userTo)
                .stream()
                .filter(a -> a.getId().equals(accountIdTo))
                .findAny()
                .orElseThrow(() -> new ThereIsNoAccountsWithId(String.format(NO_ACCOUNTS_WITH_ID, userTo.getUsername(), accountIdTo)));
    }

}
