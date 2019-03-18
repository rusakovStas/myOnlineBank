package com.stasdev.backend.endpoints;

import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;
import com.stasdev.backend.model.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping("/users")
public class Users {

    @Autowired
    UsersService usersService;

    @GetMapping("/all")
    List<ApplicationUser> getUsers(){
        return usersService.getUsers();
    }

    @PostMapping
    ApplicationUser createUser(@RequestBody ApplicationUser user){
        return usersService.createUser(user);
    }

    @DeleteMapping(params = {"username"})
    void deleteUser(@RequestParam String username){
        ofNullable(username).ifPresent(u -> usersService.deleteUserByUserName(u));
    }

}