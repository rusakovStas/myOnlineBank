package com.stasdev.backend.model.services;

import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;

import java.util.List;
import java.util.Set;

public interface UsersService {
    ApplicationUser createUser(ApplicationUser user);

    Set<Role> getUserRole(String username);

    List<ApplicationUser> getUsers();

    void deleteUserByUserName(String userName);

}
