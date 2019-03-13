package com.stasdev.backend.endpoints;

import com.stasdev.backend.errors.NotImplementedYet;
import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.Suggestion;
import com.stasdev.backend.model.entitys.Transaction;
import com.stasdev.backend.model.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class Accounts {

    @Autowired
    private AccountService accountService;

    @GetMapping("/all")
    List<Account> getAll(){
        return accountService.getAll();
    }

    @GetMapping("/my")
    List<Account> getMyAccount(Authentication authentication){
        return accountService.getMyAccount(authentication.getName());
    }

    @PostMapping("/create")
    Account createAccount(@RequestBody Account account){
        throw new NotImplementedYet("Operation 'Create' not implemented yet");
    }

    @PostMapping("/name")
    Account nameAccount(@RequestBody Account account){return null;}

    @DeleteMapping("/delete/{id}")
    void deleteAccount(@PathVariable Long id){

    }

    @PostMapping("/transaction")
    void transaction(@RequestBody Transaction transaction){
        accountService.transaction(transaction);
    }

    @GetMapping("/suggestions")
    List<Suggestion> getSuggestion(Authentication authentication){
        return accountService.getSuggestions(authentication.getName());
    }
}
