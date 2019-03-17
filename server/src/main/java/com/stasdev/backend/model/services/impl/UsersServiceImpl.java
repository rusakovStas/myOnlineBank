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
import com.stasdev.backend.model.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UsersServiceImpl implements UsersService {

    private static final String INITIAL_AMOUNT_SUM = "1000";
    private final ApplicationUserRepository repository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public UsersServiceImpl(ApplicationUserRepository repository, BCryptPasswordEncoder bCryptPasswordEncoder, RoleRepository roleRepository, AccountRepository accountRepository) {
        this.repository = repository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public ApplicationUser createUser(ApplicationUser user){
        if (repository.findByUsername(user.getUsername()) != null){
            throw new UserIsAlreadyExist(String.format("User with name '%s' already exists!", user.getUsername()));
        }
        Role userRole = roleRepository.findByRole("user").orElse(new Role("user"));
        ApplicationUser newUser = repository.saveAndFlush(
                new ApplicationUser(user.getUsername(),
                        bCryptPasswordEncoder.encode(user.getPassword()),
                        Collections.singleton(userRole)));
        Account account = new Account(
                new Amount("RUR", new BigDecimal(INITIAL_AMOUNT_SUM)),
                "3122 3123 1231 3131", /*TODO сделать генерацию*/
                "Default",
                 newUser);
        accountRepository.saveAndFlush(account);
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
        ApplicationUser byUsername = repository.findByUsername(userName);
        if (byUsername == null){
            throw new UserNotFound(String.format("User with name '%s' not found!", userName));
        }
        if (byUsername.getUsername().equals("admin")){
            throw new AdminDeleteForbidden("You can not delete admin!");
        }
        repository.deleteById(byUsername.getUser_id());
    }

    @Override
    public void deleteUserById(Long id) {
        ApplicationUser byUsername = repository.findById(id).orElseThrow(() -> new UserNotFound(String.format("User with name '%d' not found!", id)));
        if (byUsername.getUsername().equals("admin")){
            throw new AdminDeleteForbidden("You can not delete admin!");
        }
        repository.deleteById(byUsername.getUser_id());
    }
}
