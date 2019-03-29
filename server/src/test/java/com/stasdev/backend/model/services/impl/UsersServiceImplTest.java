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
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UsersServiceImplTest {

    private static final String INITIAL_AMOUNT_SUM = "1000";

    private final Preparer preparer = mock(Preparer.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final ApplicationUserRepository repository = mock(ApplicationUserRepository.class);

    private UsersServiceImpl usersService = new UsersServiceImpl(
            repository,
            roleRepository,
            accountRepository,
            preparer);

    @Test
    void testUserCanNotBeCreatedWithNameOfCreatedUser() {
//      Arrange
        String userName = "user";
        ApplicationUser alreadyExistingUser = new ApplicationUser();
        alreadyExistingUser.setUsername(userName);
        when(repository.findByUsername(alreadyExistingUser.getUsername())).thenReturn(alreadyExistingUser);
//      Act
        UserIsAlreadyExist userIsAlreadyExist = assertThrows(UserIsAlreadyExist.class, () -> usersService.createUser(alreadyExistingUser));
//      Assert
        assertThat(userIsAlreadyExist.getMessage(), is(equalTo(String.format("User with name '%s' already exists!", alreadyExistingUser.getUsername()))));
        verify(repository, times(0)).saveAndFlush(any());
        verify(accountRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testUserCanBeCreated() {
//      Arrange
        ApplicationUser userForSave = new ApplicationUser("userName", "pass");
        ApplicationUser preparedToSaveUser = userForSave.withPassword("Encoded password");
        Role userRole = new Role("user");

        when(roleRepository.findByRole("user")).thenReturn(Optional.of(userRole));
        when(preparer.prepareToSave(userForSave, userRole)).thenReturn(preparedToSaveUser);
        when(repository.findByUsername(userForSave.getUsername()))
                .thenReturn(null)
                .thenReturn(userForSave);
        when(repository.saveAndFlush(preparedToSaveUser))
                .thenReturn(preparedToSaveUser);
        Account accountForSave = getAccountForSave(preparedToSaveUser);
        when(preparer.prepareToSave(accountForSave)).thenReturn(accountForSave);
//      Act
        usersService.createUser(userForSave);
//      Assert
        verify(preparer, times(1)).prepareToSave(userForSave, userRole);
        verify(accountRepository, times(1)).saveAndFlush(accountForSave);
        verify(repository, times(2)).findByUsername(userForSave.getUsername());
    }


    @Test
    void testDeleteUserByUserName() {
//      Arrange
        ApplicationUser userForCheckDelete = new ApplicationUser("user for check delete", "pass");
        when(repository.findByUsername(userForCheckDelete.getUsername())).thenReturn(userForCheckDelete);
        when(roleRepository.findByRole("admin")).thenReturn(Optional.of(new Role("admin")));
        List<Account> accountsByUser = Arrays.asList(getAccountForSave(userForCheckDelete).withId(1L), new Account().withId(2L));
        when(accountRepository.findAccountsByUser(userForCheckDelete)).thenReturn(accountsByUser);
//      Act
        usersService.deleteUserByUserName(userForCheckDelete.getUsername());
//      Assert
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(2)).delete(accountArgumentCaptor.capture());
        assertThat(accountArgumentCaptor.getAllValues(), equalTo(accountsByUser));
        verify(repository, times(1)).deleteById(userForCheckDelete.getUser_id());
        verify(repository, times(1)).flush();
    }

    @Test
    void testAdminCanNotBeDeleted() {
//      Arrange
        Role adminRole = new Role("admin");
        ApplicationUser userForCheckDeleteAdmin = new ApplicationUser("user for check delete admin", "pass",Collections.singleton(adminRole) ,Collections.singleton(new Account()));
        when(repository.findByUsername(userForCheckDeleteAdmin.getUsername())).thenReturn(userForCheckDeleteAdmin);
        when(roleRepository.findByRole("admin")).thenReturn(Optional.of(adminRole));
//      Act
        AdminDeleteForbidden adminDeleteForbidden = assertThrows(AdminDeleteForbidden.class, () -> usersService.deleteUserByUserName(userForCheckDeleteAdmin.getUsername()));
//      Arrange
        assertThat(adminDeleteForbidden.getMessage(), is("You can not delete admin!"));
        verify(accountRepository, times(0)).delete(any());
        verify(repository, times(0)).deleteById(anyLong());
    }

    @Test
    void testExceptionIfRoleAdminNotExist() {
//      Arrange
        when(roleRepository.findByRole("admin")).thenReturn(Optional.empty());
        ApplicationUser someUser = new ApplicationUser("some name", "pass");
        when(repository.findByUsername(someUser.getUsername())).thenReturn(someUser);
//      Act
        RuntimeException roleAdminNotExists = assertThrows(RuntimeException.class, () -> usersService.deleteUserByUserName(someUser.getUsername()));
//      Assert
        assertThat(roleAdminNotExists.getMessage(), is("Not have role admin"));
        verify(accountRepository, times(0)).delete(any());
        verify(repository, times(0)).deleteById(anyLong());
    }

    @Test
    void testExceptionIfUserNotExists() {
//      Arrange
        ApplicationUser userWhichNotExist = new ApplicationUser("user which not exist", "pass");
        when(repository.findByUsername(userWhichNotExist.getUsername())).thenReturn(null);
//      Act
        UserNotFound userNotFound = assertThrows(UserNotFound.class, () -> usersService.deleteUserByUserName(userWhichNotExist.getUsername()));
//      Assert
        assertThat(userNotFound.getMessage(), is(String.format("User with name '%s' not found!", userWhichNotExist.getUsername())));
        verify(accountRepository, times(0)).delete(any());
        verify(repository, times(0)).deleteById(anyLong());
    }

    private Account getAccountForSave(ApplicationUser userPreparedToSave) {
        return new Account(
                new Amount("RUR", new BigDecimal(INITIAL_AMOUNT_SUM)),
                "Default",
                userPreparedToSave);
    }

}