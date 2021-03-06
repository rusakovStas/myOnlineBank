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

    /*
    TODO вообще то сюда достаточно скинуть чисто имя юзера (что по факту и будет), но на будущее оставлю здесь Account
    * */
    @PostMapping
    Account createAccount(@RequestBody Account account){
        return accountService.createAccount(account);
    }

    @PutMapping
    Account nameAccount(Authentication authentication, @RequestBody Account account){
        return accountService.nameAccount(authentication.getName(), account);
    }

    @DeleteMapping(params = {"id"})
    void deleteAccount(Authentication authentication, @RequestParam Long id){
        accountService.deleteAccount(authentication.getName(), id);
    }

    @PostMapping("/transaction")
    void transaction(@RequestBody Transaction transaction, Authentication authentication){
        String name = authentication.getName();
        accountService.transaction(transaction, name);
    }

    @GetMapping(value = "/suggestions")
    List<Suggestion> getSuggestion(Authentication authentication, @RequestParam(required = false) Long excludeAccountId){
        return accountService.getSuggestions(authentication.getName(), excludeAccountId);
    }
}
