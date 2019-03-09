package com.stasdev.backend.model.services.impl;

import com.stasdev.backend.errors.AdminDeleteForbidden;
import com.stasdev.backend.errors.UserIsAlreadyExist;
import com.stasdev.backend.errors.UserNotFound;
import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;
import com.stasdev.backend.model.repos.AccountRepository;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import com.stasdev.backend.model.repos.RoleRepository;
import com.stasdev.backend.model.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class UsersServiceImpl implements UsersService {

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
        accountRepository.saveAndFlush(new Account());
        return repository.saveAndFlush(
                new ApplicationUser(user.getUsername(),
                bCryptPasswordEncoder.encode(user.getPassword()),
                Collections.singleton(userRole))
//                        Collections.singleton())
        );
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
}
