package com.stasdev.backend.model.services.impl;

import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;
import com.stasdev.backend.model.services.Preparer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class PreparerImpl implements Preparer {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public static final String prefixOfAccountNumber = "5469 0600";

    @Autowired
    public PreparerImpl(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public ApplicationUser prepareToSave(ApplicationUser userForSave, Role userRole){
        return new ApplicationUser(userForSave.getUsername(),
                bCryptPasswordEncoder.encode(userForSave.getPassword()),
                Collections.singleton(userRole));
    }

    @Override
    public Account prepareToSave(final Account account){
        Random random = new Random();
        List<String> firstNumber = random
                .ints(4, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
        List<String> secondNumber = random
                .ints(4, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());

        String finalNumber = String.format("%s %s %s", prefixOfAccountNumber, String.join("", firstNumber), String.join("", secondNumber));
        account.setNumber(finalNumber);
        return account;
    }

}
