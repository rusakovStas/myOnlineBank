package com.stasdev.backend.model.services.impl;

import com.stasdev.backend.errors.AdminDeleteForbidden;
import com.stasdev.backend.errors.UserIsAlreadyExist;
import com.stasdev.backend.errors.UserNotFound;
import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.Amount;
import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;
import com.stasdev.backend.model.repos.AccountRepository;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import com.stasdev.backend.model.repos.RoleRepository;
import com.stasdev.backend.model.services.Preparer;
import com.stasdev.backend.model.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Optional.ofNullable;

@Service
public class UsersServiceImpl implements UsersService {

    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final ApplicationUserRepository repository;
    private final Preparer preparer;
    private static final String INITIAL_AMOUNT_SUM = "1000";


    @Autowired
    public UsersServiceImpl(ApplicationUserRepository repository, RoleRepository roleRepository, AccountRepository accountRepository, Preparer preparer) {
        this.repository = repository;
        this.preparer = preparer;
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public ApplicationUser createUser(ApplicationUser user){
        if (repository.findByUsername(user.getUsername()) != null){
            throw new UserIsAlreadyExist(String.format("User with name '%s' already exists!", user.getUsername()));
        }
        Role userRole = roleRepository.findByRole("user").orElse(new Role("user"));
        ApplicationUser newUser = repository.saveAndFlush(preparer.prepareToSave(user, userRole));
        Account account = new Account(
                new Amount("RUR", new BigDecimal(INITIAL_AMOUNT_SUM)),
                "Default",
                 newUser);
        accountRepository.saveAndFlush(preparer.prepareToSave(account));
        return repository.findByUsername(newUser.getUsername());
    }


    @Override
    public Set<Role> getUserRole(String username){
        return repository.findByUsername(username).getRoles();
    }

    @Override
    public List<ApplicationUser> getUsers(){
        return repository.findAll();
    }

    @Override
    public void deleteUserByUserName(String userName) {
        ApplicationUser byUsername = ofNullable(repository.findByUsername(userName)).orElseThrow(() -> new UserNotFound(String.format("User with name '%s' not found!", userName)));
        Role admin = roleRepository.findByRole("admin").orElseThrow(() -> new RuntimeException("Not have role admin"));
        if (byUsername.getRoles().contains(admin)){
            throw new AdminDeleteForbidden("You can not delete admin!");
        }
        accountRepository
                .findAccountsByUser(byUsername)
                .forEach(accountRepository::delete);
        repository.deleteById(byUsername.getUser_id());
        repository.flush();
    }

}
