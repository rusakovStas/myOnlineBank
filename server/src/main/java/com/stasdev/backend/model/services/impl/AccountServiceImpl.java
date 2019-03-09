package com.stasdev.backend.model.services.impl;

import com.stasdev.backend.errors.NotImplementedYet;
import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.Transaction;
import org.omg.CORBA.NO_IMPLEMENT;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Service
@RequestMapping("/accounts")
public class AccountServiceImpl {

    @GetMapping("/all")
    List<Account> getAll(){
        return null;
    }

    @GetMapping("/my")
    List<Account> getMyAccount(Authentication authentication){
        String name = authentication.getName();

        return null;
    }

    @PostMapping("/create")
    Account createAccount(@RequestBody Account account){
        throw new NotImplementedYet("Operation 'Create' not implemented yet");
    }

    @PostMapping("/name")
    Account nameAccount(@RequestBody Account account){
        return null;
    }

    @DeleteMapping("/delete/{id}")
    void deleteAccount(@PathVariable Long id){

    }

    @PostMapping("/transaction")
    boolean transaction(@RequestBody Transaction transaction){
        return false;
    }
}
