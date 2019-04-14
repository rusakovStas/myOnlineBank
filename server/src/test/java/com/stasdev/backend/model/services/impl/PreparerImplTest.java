package com.stasdev.backend.model.services.impl;

import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PreparerImplTest {

    private static final String ENCODED_PASSWORD = "Encoded password";
    private final BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
    private final PreparerImpl preparer = new PreparerImpl(bCryptPasswordEncoder);


    @Test
    void testPrepareToSaveUser() {
//      Arrange
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        ApplicationUser userForPrepare = new ApplicationUser("userName", "pass");
        Role userRole = new Role("user");
//      Act
        ApplicationUser applicationUser = preparer.prepareToSave(userForPrepare, userRole);
//      Assert
        assertThat(applicationUser, equalTo(getPreparedToSaveUser(userForPrepare, userRole)));
        verify(bCryptPasswordEncoder, times(1)).encode(userForPrepare.getPassword());
    }

    private ApplicationUser getPreparedToSaveUser(ApplicationUser user, Role role){
        return new ApplicationUser(user.getUsername(), ENCODED_PASSWORD, Collections.singleton(role));
    }

    @Test
    void testPrepareToSaveAccount() {
//      Arrange
        Account account = new Account();
        account.setNumber(null);
//      Act
        Account accountAfterPrepare = preparer.prepareToSave(account);
//      Assert
        assertThat(accountAfterPrepare.getNumber().matches(String.format("%s \\d{4} \\d{4}", PreparerImpl.prefixOfAccountNumber)), is(true));
    }
}