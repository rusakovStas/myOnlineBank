package api;

import com.stasdev.backend.errors.NotImplementedYet;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/*
* Accounts API tests
* Проверяем защищенность всех ендпоинтов
* и тестируем все операции
* */
class AccountsControllerTest extends CommonApiTest {

    @Test
    void allEndpointsSecured() {
        ResponseEntity<String> allAccounts = nonAuth().restClientWithoutErrorHandler().getForEntity("/accounts/all", String.class);
        assertThat(allAccounts.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> myAccounts = nonAuth().restClientWithoutErrorHandler().getForEntity("/accounts/my", String.class);
        assertThat(myAccounts.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> createAccount = nonAuth().restClientWithoutErrorHandler().postForEntity("/accounts/create",null, String.class);
        assertThat(createAccount.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> transaction = nonAuth().restClientWithoutErrorHandler().postForEntity("/accounts/transaction",null, String.class);
        assertThat(transaction.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void userHasDefaultAccount() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void userCanSeeHisAccounts() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void userCanNotSeeAccountsAnotherUsers() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void adminCanSeeAllAccounts() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void userCanDoTransaction() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void userCanNotDoTransactionWithAmountMoreThenHeHaveOnAccount() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void userCanNotDoTransactionToUserWhichNotExists() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void userCanNotDoTransactionWithDifferentCurrencies() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void userCanNotDoTransactionNotFromTheirAccounts() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void adminCanDoTransactionFromAnyAccounts() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void userCanDeleteHisAccount() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void userCanNotDeleteAccountsAnotherUsers() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void adminCanDeleteAnyAccount() {
        throw new NotImplementedYet("Not implemented");
    }

    @Test
    void adminHasDefaultAccountWithInfinityAmount() {
        throw new NotImplementedYet("Not implemented");
    }
}
