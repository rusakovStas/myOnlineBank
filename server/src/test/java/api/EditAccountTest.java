package api;

import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.ApplicationUser;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EditAccountTest extends CommonApiTest {

    public static final String YOU_DON_T_HAS_ACCOUNT_WITH_ID = "You don't has account with id %d";

    @Test
    void userCanRenameHisOwnAccount() {
        List<Account> accountsOfDefaultUser = getAccountsOfDefaultUser();
        Account firstAccount = accountsOfDefaultUser.get(0);
        firstAccount.setAmount(null);
        firstAccount.setNumber(null);
        String renamedAccountName = "RenamedAccount";
        firstAccount.setName(renamedAccountName);

        ResponseEntity<Account> updatedAccountRs = authUser().restClientWithoutErrorHandler().exchange("/accounts", HttpMethod.PUT, new HttpEntity<>(firstAccount), Account.class);

        assertThat(updatedAccountRs.getStatusCode(), is(HttpStatus.OK));
        Account updatedAccount = updatedAccountRs.getBody();
        assert updatedAccount != null;
        assertThat(updatedAccount.getName(), is(renamedAccountName));
    }

    @Test
    void userCanNameNotNamedAccount() {
        Account accountForCreation = new Account();
        String userName = "userForCheckRenameAccount";
        ApplicationUser userForCheckNameOfAccount = createUser(userName);
        accountForCreation.setUser(userForCheckNameOfAccount);

        ResponseEntity<Account> createdAccountAsResponse = authByUser(userName, DEFAULT_PASSWORD).restClientWithoutErrorHandler().postForEntity("/accounts", accountForCreation, Account.class);

        Account createdAccount = createdAccountAsResponse.getBody();
        assert createdAccount != null;
        String renamedAccountName = "Name for new account";
        createdAccount.setName(renamedAccountName);

        ResponseEntity<Account> updatedAccountRs = authByUser(userName, DEFAULT_PASSWORD).restClientWithoutErrorHandler().exchange("/accounts", HttpMethod.PUT, new HttpEntity<>(createdAccount), Account.class);

        assertThat(updatedAccountRs.getStatusCode(), is(HttpStatus.OK));
        Account updatedAccount = updatedAccountRs.getBody();
        assert updatedAccount != null;
        assertThat(updatedAccount.getName(), is(renamedAccountName));
    }

    @Test
    void userCanNotRenameNotHisAccount() {
        Account accountForCreation = new Account();
        String userName = "notDefaultUser";
        ApplicationUser userForCheckNameOfAccount = createUser(userName);
        accountForCreation.setUser(userForCheckNameOfAccount);

        ResponseEntity<Account> createdAccountAsResponse = authByUser(userName, DEFAULT_PASSWORD).restClientWithoutErrorHandler().postForEntity("/accounts", accountForCreation, Account.class);

        Account createdAccount = createdAccountAsResponse.getBody();
        assert createdAccount != null;
        String renamedAccountName = "Name for new account";
        createdAccount.setName(renamedAccountName);

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authUser().restClientWithErrorHandler().exchange("/accounts", HttpMethod.PUT, new HttpEntity<>(createdAccount), Account.class));
        assertThat(runtimeException.getMessage(), containsString(String.format(YOU_DON_T_HAS_ACCOUNT_WITH_ID, createdAccount.getId())));
    }

    @Test
    void adminCanNotRenameNotHisOwnAccount() {
        Account accountForCreation = new Account();
        String userName = "notAdminUser";
        ApplicationUser userForCheckNameOfAccount = createUser(userName);
        accountForCreation.setUser(userForCheckNameOfAccount);

        ResponseEntity<Account> createdAccountAsResponse = authByUser(userName, DEFAULT_PASSWORD).restClientWithoutErrorHandler().postForEntity("/accounts", accountForCreation, Account.class);

        Account createdAccount = createdAccountAsResponse.getBody();
        assert createdAccount != null;
        String renamedAccountName = "Name for new account";
        createdAccount.setName(renamedAccountName);

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authAdmin().restClientWithErrorHandler().exchange("/accounts", HttpMethod.PUT, new HttpEntity<>(createdAccount), Account.class));
        assertThat(runtimeException.getMessage(), containsString(String.format(YOU_DON_T_HAS_ACCOUNT_WITH_ID, createdAccount.getId())));
    }

    @Test
    void adminCanRenameHisOwnAccount() {
        List<Account> accountsOfAdminUser = getAccountsOfAdminUser();
        Account firstAccount = accountsOfAdminUser.get(0);
        firstAccount.setAmount(null);
        firstAccount.setNumber(null);
        String renamedAccountName = "RenamedAccount";
        firstAccount.setName(renamedAccountName);

        ResponseEntity<Account> updatedAccountRs = authAdmin().restClientWithoutErrorHandler().exchange("/accounts", HttpMethod.PUT, new HttpEntity<>(firstAccount), Account.class);

        assertThat(updatedAccountRs.getStatusCode(), is(HttpStatus.OK));
        Account updatedAccount = updatedAccountRs.getBody();
        assert updatedAccount != null;
        assertThat(updatedAccount.getName(), is(renamedAccountName));
    }
}
