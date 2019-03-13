package api;

import com.stasdev.backend.errors.NotImplementedYet;
import com.stasdev.backend.model.entitys.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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

        ResponseEntity<String> suggestions = nonAuth().restClientWithoutErrorHandler().postForEntity("/accounts/suggestions",null, String.class);
        assertThat(suggestions.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void userHasDefaultAccount() {
        ResponseEntity<ApplicationUser> userHasDefaultAcc = createUserByAdmin("userHasDefaultAcc");
        ApplicationUser user = userHasDefaultAcc.getBody();
        assert user != null;
        ResponseEntity<List<Account>> userAccountsEntity = authByUser(user.getUsername(), DEFAULT_PASSWORD)
                .restClientWithoutErrorHandler().exchange("/accounts/my", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts != null;
        assertThat(accounts.size(), is(1));
        Account account = accounts.get(0);

        BigDecimal expected = new BigDecimal(new BigInteger("1000"));
        expected = expected.setScale(2);

        assertThat(account.getName(), is("Default"));
        assertThat(account.getAmount().getSum(), equalTo(expected));
        assertThat(account.getAmount().getCurrency(), is("RUR"));
    }

    @Test
    void userCanSeeHisAccounts() {
        ResponseEntity<List<Account>> userAccountsEntity = authUser()
                .restClientWithoutErrorHandler().exchange("/accounts/my", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts != null;
        assertThat(accounts.size(), is(1));
        Account account = accounts.get(0);

        BigDecimal expected = new BigDecimal(new BigInteger("123"));
        expected = expected.setScale(2);

        assertThat(account.getName(), is(""));
        assertThat(account.getAmount().getSum(), equalTo(expected));
        assertThat(account.getAmount().getCurrency(), is("RUR"));

    }

    @Test
    void userCanNotSeeAccountsAnotherUsers() {
        ResponseEntity<String> all = authUser()
                .restClientWithoutErrorHandler()
                .getForEntity("/accounts/all", String.class);

        assertThat(all.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void adminCanSeeAllAccounts() {
        ResponseEntity<List<Account>> userAccountsEntity = authAdmin()
                .restClientWithoutErrorHandler().exchange("/accounts/all", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts != null;
        assertThat(accounts.size(), greaterThanOrEqualTo(3)); //Три счета (в сумме) создаются при запуске
    }

    /*
    * Создаем юзера
    * Под дефолтным юзером отправляем транзацию на созданного
    *
    * */
    @Test
    void userCanDoTransaction() {
        ApplicationUser userForCheckTransaction = createUser("UserForCheckTransaction");
        Account accountFromForTransaction = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromBeforeTransh = accountFromForTransaction.getAmount().getSum();
        Account accountToForTransh = getAccountsOfCreatedUser(userForCheckTransaction.getUsername()).get(0);
        BigDecimal sumToBeforeTransh = accountToForTransh.getAmount().getSum();

        BigDecimal amountOfTransh = new BigDecimal(new BigInteger("10"));
        
        Transaction transaction = new Transaction();
        transaction.setAccountIdFrom(accountFromForTransaction.getId());
        transaction.setAccountNumberFrom(accountFromForTransaction.getNumber());
        transaction.setUserFrom(accountFromForTransaction.getUser().getUsername());
        transaction.setAmount(new Amount("RUR", amountOfTransh));

        Suggestion anotherUser = getSuggestionsToDefaultUser()
                .stream()
                .filter(s -> s.getUserName().equals(userForCheckTransaction.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("no created user for transaction"));

        transaction.setAccountIdTo(anotherUser.getAccountId());
        transaction.setUserTo(anotherUser.getUserName());

        authUser().restClientWithoutErrorHandler().postForEntity("/accounts/transaction", transaction, String.class);

        Account accountDef = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromAfter = accountDef.getAmount().getSum();

        Account accountUser = getAccountsOfCreatedUser(userForCheckTransaction.getUsername()).get(0);
        BigDecimal sumToAfter = accountUser.getAmount().getSum();

        assertThat(sumFromBeforeTransh.subtract(amountOfTransh), equalTo(sumFromAfter));
        assertThat(sumToBeforeTransh.add(amountOfTransh), equalTo(sumToAfter));
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

    @Test
    void userCanGetSuggestionsForTransaction() {
        ResponseEntity<List<Suggestion>> suggestionsEntity = authUser()
                .restClientWithoutErrorHandler()
                .exchange("/accounts/suggestions", HttpMethod.GET,null, new ParameterizedTypeReference<List<Suggestion>>(){});
        assertThat(suggestionsEntity.getStatusCode(), is(HttpStatus.OK));
        List<Suggestion> suggestions = suggestionsEntity.getBody();
        assert suggestions != null;
        /*Проверить что все suggestion - равны количеству счетов*/
        /*Проверить что счета замаскированы*/
        /*Проверить что имя юзера который делает запрос заменено на My own account*/
        throw new NotImplementedYet("Not implemented");

    }



    private List<Account> getAccountsOfDefaultUser(){
        ResponseEntity<List<Account>> userAccountsEntity = authUser()
                .restClientWithoutErrorHandler().exchange("/accounts/my", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts!=null;
        return accounts;
    }

    private List<Account> getAccountsOfCreatedUser(String user){
        ResponseEntity<List<Account>> userAccountsEntity = authByUser(user, DEFAULT_PASSWORD)
                .restClientWithoutErrorHandler().exchange("/accounts/my", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts!=null;
        return accounts;
    }

    private List<Suggestion> getSuggestionsToDefaultUser(){
        ResponseEntity<List<Suggestion>> suggestionsEntity = authUser()
                .restClientWithoutErrorHandler()
                .exchange("/accounts/suggestions", HttpMethod.GET,null, new ParameterizedTypeReference<List<Suggestion>>(){});

        List<Suggestion> suggestions = suggestionsEntity.getBody();
        assert suggestions != null;
        return suggestions;
    }

    private List<Suggestion> getSuggestionsToCreatedUser(String user){
        ResponseEntity<List<Suggestion>> suggestionsEntity = authByUser(user,DEFAULT_PASSWORD)
                .restClientWithoutErrorHandler()
                .exchange("/accounts/suggestions", HttpMethod.GET,null, new ParameterizedTypeReference<List<Suggestion>>(){});

        List<Suggestion> suggestions = suggestionsEntity.getBody();
        assert suggestions != null;
        return suggestions;
    }

    private ApplicationUser createUser(String userName){
        ResponseEntity<ApplicationUser> userByAdmin = createUserByAdmin(userName);
        ApplicationUser body = userByAdmin.getBody();
        assert body!=null;
        return body;
    }
}
