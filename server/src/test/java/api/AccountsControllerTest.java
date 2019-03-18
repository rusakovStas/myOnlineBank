package api;

import com.stasdev.backend.errors.NotImplementedYet;
import com.stasdev.backend.model.entitys.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
* Accounts API tests
* Проверяем защищенность всех ендпоинтов
* и тестируем все операции
* TODO придумать удаление юзера после теста - возможно нужно организовать "отмену команды"
* */
class AccountsControllerTest extends CommonApiTest {

    private static final String NOT_ENOUGH_MONEY_ERROR = "On account with number %s amount of money is %s and that not enough for transaction with amount %s";
    private static final String NO_ACCOUNTS_WITH_ID_ERROR = "User '%s' have no account with id %d";
    private static final String DIFFERENT_CURRENCY_ERROR = "Comparison between different currency not implemented yet";
    private static final String YOU_DON_T_HAS_ACCOUNT_WITH_ID = "You don't has account with id %d";
    private static final String USER_HASN_T_ROLE_ADMIN_AND_CAN_T_DO_TRANSACTION_FROM_NOT_HIS_ACCOUNTS = "User '%s' hasn't role admin and can't do transaction from not his accounts";

    @Test
    void allEndpointsSecured() {
        ResponseEntity<String> allAccounts = nonAuth().restClientWithoutErrorHandler().getForEntity("/accounts/all", String.class);
        assertThat(allAccounts.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> myAccounts = nonAuth().restClientWithoutErrorHandler().getForEntity("/accounts/my", String.class);
        assertThat(myAccounts.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> createAccount = nonAuth().restClientWithoutErrorHandler().postForEntity("/accounts",null, String.class);
        assertThat(createAccount.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> deleteAccount = nonAuth().restClientWithoutErrorHandler().exchange("/accounts?id=1", HttpMethod.DELETE, null, String.class);
        assertThat(deleteAccount.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> transaction = nonAuth().restClientWithoutErrorHandler().postForEntity("/accounts/transaction",null, String.class);
        assertThat(transaction.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> suggestions = nonAuth().restClientWithoutErrorHandler().postForEntity("/accounts/suggestions",null, String.class);
        assertThat(suggestions.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void userHasDefaultAccount() {
        ApplicationUser user = createUser("userHasDefaultAcc");
        List<Account> accounts = getAccountsOfCreatedUser(user.getUsername());
        assertThat(accounts.size(), is(1));
        Account account = accounts.get(0);

        BigDecimal expected = new BigDecimal(new BigInteger("1000"));
        expected = expected.setScale(2);

        assertThat(account.getName(), is("Default"));
        assertThat(account.getAmount().getSum(), equalTo(expected));
        assertThat(account.getAmount().getCurrency(), is("RUR"));
    }

    /*
    * количество денег на счету не проверяем потому что оно может измениться в других тестах
    * */
    @Test
    void userCanSeeHisAccounts() {
        List<Account> accounts = getAccountsOfDefaultUser();
        assertThat(accounts.size(), is(2));
        Account account = accounts.get(1);

        assertThat(account.getName(), is(""));
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

    @Test
    void userCanDoTransaction() {
        ApplicationUser userTo = createUser("userTo");
        Account accFrom = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromBefore = accFrom.getAmount().getSum();
        Account accTo = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToBefore = accTo.getAmount().getSum();

        BigDecimal amountOfTransh = new BigDecimal(new BigInteger("10"));

        Transaction transaction = new Transaction();
        transaction.setAccountIdFrom(accFrom.getId());
        transaction.setAccountNumberFrom(accFrom.getNumber());
        transaction.setUserFrom(accFrom.getUser().getUsername());
        transaction.setAmount(new Amount("RUR", amountOfTransh));

        Suggestion anotherUser = getSuggestionsToDefaultUser()
                .stream()
                .filter(s -> s.getUserName().equals(userTo.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("no created user for transaction"));

        transaction.setAccountIdTo(anotherUser.getAccountId());
        transaction.setUserTo(anotherUser.getUserName());

        authUser().restClientWithoutErrorHandler().postForEntity("/accounts/transaction", transaction, String.class);

        Account accountDef = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromAfter = accountDef.getAmount().getSum();

        Account accountUser = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToAfter = accountUser.getAmount().getSum();

        assertThat(sumFromBefore.subtract(amountOfTransh), equalTo(sumFromAfter));
        assertThat(sumToBefore.add(amountOfTransh), equalTo(sumToAfter));
    }

    @Test
    void userCanNotDoTransactionWithAmountMoreThenHeHasOnAccount() {
        ApplicationUser userTo = createUser("userToForAmountCheck");
        Account accFrom = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromBefore = accFrom.getAmount().getSum();
        Account accTo = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToBefore = accTo.getAmount().getSum();

        BigDecimal amountOfTransh = sumFromBefore.add(new BigDecimal("10"));

        Transaction transaction = new Transaction();
        transaction.setAccountIdFrom(accFrom.getId());
        transaction.setAccountNumberFrom(accFrom.getNumber());
        transaction.setUserFrom(accFrom.getUser().getUsername());
        transaction.setAmount(new Amount("RUR", amountOfTransh));

        Suggestion anotherUser = getSuggestionsToDefaultUser()
                .stream()
                .filter(s -> s.getUserName().equals(userTo.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("no created user for transaction"));

        transaction.setAccountIdTo(anotherUser.getAccountId());
        transaction.setUserTo(anotherUser.getUserName());



        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authUser()
                        .restClientWithErrorHandler()
                        .postForEntity("/accounts/transaction", transaction, String.class));
        assertThat(runtimeException.getMessage(), containsString(String.format(NOT_ENOUGH_MONEY_ERROR, accFrom.getNumber(), accFrom.getAmount().getSum(), amountOfTransh)));
    }

    @Test
    void userCanNotDoTransactionToAccountWhichNotExists() {
        Account accFrom = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromBefore = accFrom.getAmount().getSum();

        BigDecimal amountOfTransh = sumFromBefore.add(new BigDecimal("10"));

        Transaction transaction = new Transaction();
        transaction.setAccountIdFrom(accFrom.getId());
        transaction.setAccountNumberFrom(accFrom.getNumber());
        transaction.setUserFrom(accFrom.getUser().getUsername());
        transaction.setAmount(new Amount("RUR", amountOfTransh));

        Long maxId = getSuggestionsToDefaultUser()
                .stream()
                .mapToLong(Suggestion::getAccountId)
                .max()
                .orElseThrow(() -> new RuntimeException("didn't find a max element"));

        transaction.setAccountIdTo(maxId+1L);
        transaction.setUserTo(accFrom.getUser().getUsername());

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authUser()
                        .restClientWithErrorHandler()
                        .postForEntity("/accounts/transaction", transaction, String.class));
        assertThat(runtimeException.getMessage(), containsString(String.format(NO_ACCOUNTS_WITH_ID_ERROR,accFrom.getUser().getUsername() ,maxId+1L)));
    }

    @Test
    void userCanNotDoTransactionWithDifferentCurrencies() {
        ApplicationUser userTo = createUser("userToWithDifferentCurrency");
        Account accFrom = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromBefore = accFrom.getAmount().getSum();

        BigDecimal amountOfTransh = sumFromBefore.add(new BigDecimal("10"));

        Transaction transaction = new Transaction();
        transaction.setAccountIdFrom(accFrom.getId());
        transaction.setAccountNumberFrom(accFrom.getNumber());
        transaction.setUserFrom(accFrom.getUser().getUsername());
        transaction.setAmount(new Amount("USD", amountOfTransh));

        Suggestion anotherUser = getSuggestionsToDefaultUser()
                .stream()
                .filter(s -> s.getUserName().equals(userTo.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("no created user for transaction"));

        transaction.setAccountIdTo(anotherUser.getAccountId());
        transaction.setUserTo(anotherUser.getUserName());


        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authUser()
                        .restClientWithErrorHandler()
                        .postForEntity("/accounts/transaction", transaction, String.class));
        assertThat(runtimeException.getMessage(), containsString(DIFFERENT_CURRENCY_ERROR));
    }

    @Test
    void userCanNotDoTransactionNotFromTheirAccounts() {
        ApplicationUser userTo = createUser("notDefaultUser");
        Account accFrom = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromBefore = accFrom.getAmount().getSum();
        Account accTo = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToBefore = accTo.getAmount().getSum();

        BigDecimal amountOfTransh = sumFromBefore.add(new BigDecimal("10"));

        Transaction transaction = new Transaction();
        transaction.setAmount(new Amount("RUR", amountOfTransh));

        Suggestion anotherUser = getSuggestionsToDefaultUser()
                .stream()
                .filter(s -> s.getUserName().equals(userTo.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("no created user for transaction"));

        transaction.setAccountIdTo(accFrom.getId());
        transaction.setAccountNumberTo(accFrom.getNumber());
        transaction.setUserTo(anotherUser.getUserName());

        transaction.setAccountIdFrom(anotherUser.getAccountId());
        transaction.setUserFrom(anotherUser.getUserName());


        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authUser()
                        .restClientWithErrorHandler()
                        .postForEntity("/accounts/transaction", transaction, String.class));
        assertThat(runtimeException.getMessage(), containsString(String.format(USER_HASN_T_ROLE_ADMIN_AND_CAN_T_DO_TRANSACTION_FROM_NOT_HIS_ACCOUNTS, anotherUser.getUserName())));
    }

    @Test
    void userCantDoTransactionFromNotExistingAccount() {
        Account accFrom = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromBefore = accFrom.getAmount().getSum();

        BigDecimal amountOfTransh = sumFromBefore.add(new BigDecimal("10"));

        Transaction transaction = new Transaction();
        transaction.setAccountNumberFrom(accFrom.getNumber());
        transaction.setUserFrom(accFrom.getUser().getUsername());
        transaction.setAmount(new Amount("RUR", amountOfTransh));

        Long maxId = getSuggestionsToDefaultUser()
                .stream()
                .mapToLong(Suggestion::getAccountId)
                .max()
                .orElseThrow(() -> new RuntimeException("didn't find a max element"));

        transaction.setAccountIdFrom(maxId+1L);
        transaction.setAccountIdTo(accFrom.getId());
        transaction.setUserTo(accFrom.getUser().getUsername());

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authUser()
                        .restClientWithErrorHandler()
                        .postForEntity("/accounts/transaction", transaction, String.class));
        assertThat(runtimeException.getMessage(), containsString(String.format(NO_ACCOUNTS_WITH_ID_ERROR,accFrom.getUser().getUsername() ,maxId+1L)));
    }

    @Test
    void adminCanDoTransactionFromAnyAccounts() {
        ApplicationUser userTo = createUser("userToWhichNotAdmin");
        Account accFrom = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromBefore = accFrom.getAmount().getSum();
        Account accTo = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToBefore = accTo.getAmount().getSum();

        BigDecimal amountOfTransh = new BigDecimal(new BigInteger("10"));

        Transaction transaction = new Transaction();
        transaction.setAccountIdFrom(accFrom.getId());
        transaction.setAccountNumberFrom(accFrom.getNumber());
        transaction.setUserFrom(accFrom.getUser().getUsername());
        transaction.setAmount(new Amount("RUR", amountOfTransh));

        Suggestion anotherUser = getSuggestionsToDefaultUser()
                .stream()
                .filter(s -> s.getUserName().equals(userTo.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("no created user for transaction"));

        transaction.setAccountIdTo(anotherUser.getAccountId());
        transaction.setUserTo(anotherUser.getUserName());

        authAdmin().restClientWithoutErrorHandler().postForEntity("/accounts/transaction", transaction, String.class);

        Account accountDef = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromAfter = accountDef.getAmount().getSum();

        Account accountUser = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToAfter = accountUser.getAmount().getSum();

        assertThat(sumFromBefore.subtract(amountOfTransh), equalTo(sumFromAfter));
        assertThat(sumToBefore.add(amountOfTransh), equalTo(sumToAfter));
    }

    @Test
    void userCanDeleteHisAccount() {
        ApplicationUser userForCheckAccountDeleting = createUser("UserForCheckAccountDeleting");
        List<Account> accountsOfCreatedUser = getAccountsOfCreatedUser(userForCheckAccountDeleting.getUsername());
        int allAccSizeBefore = getAllAccountsByAdmin().size();
        Account account = accountsOfCreatedUser.get(0);
        authByUser(userForCheckAccountDeleting.getUsername(), DEFAULT_PASSWORD)
                .restClientWithoutErrorHandler().delete("/accounts?id="+account.getId());
        int allAccSizeAfter = getAllAccountsByAdmin().size();
        List<Account> accountsAfterDeleting = getAccountsOfCreatedUser(userForCheckAccountDeleting.getUsername());
        assertThat(accountsAfterDeleting.size(), is(0));
        assertThat(allAccSizeAfter, is(allAccSizeBefore - 1));
    }

    @Test
    void userCanNotDeleteAccountsAnotherUsers() {
        ApplicationUser userForCheckAccountDeleting = createUser("UserForCheckForbiddenDeletingAccAnotherUser");
        List<Account> accountsOfDefaultUser = getAccountsOfDefaultUser();
        Account account = accountsOfDefaultUser.get(0);
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authByUser(userForCheckAccountDeleting.getUsername(), DEFAULT_PASSWORD)
                        .restClientWithErrorHandler().delete("/accounts?id=" + account.getId()));
        assertThat(runtimeException.getMessage(), containsString(String.format(YOU_DON_T_HAS_ACCOUNT_WITH_ID, account.getId())));
    }

    @Test
    void adminCanDeleteAnyAccount() {
        ApplicationUser userForCheckAccountDeleting = createUser("UserForCheckAccountDeletingByAdmin");
        List<Account> accountsOfCreatedUser = getAccountsOfCreatedUser(userForCheckAccountDeleting.getUsername());
        int allAccSizeBefore = getAllAccountsByAdmin().size();
        Account account = accountsOfCreatedUser.get(0);
        authAdmin()
                .restClientWithoutErrorHandler().delete("/accounts?id=" + account.getId());
        int allAccSizeAfter = getAllAccountsByAdmin().size();
        List<Account> accountsAfterDeleting = getAccountsOfCreatedUser(userForCheckAccountDeleting.getUsername());
        assertThat(accountsAfterDeleting.size(), is(0));
        assertThat(allAccSizeAfter, is(allAccSizeBefore - 1));
    }

    @Test
    void adminHasDefaultAccountWithInfinityAmount() {
        ApplicationUser userTo = createUser("userForCheckAdminInfinityAccount");
        Account accFrom = getAccountsOfAdminUser().get(0);
        BigDecimal sumFromBefore = accFrom.getAmount().getSum();
        Account accTo = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToBefore = accTo.getAmount().getSum();

        BigDecimal amountOfTransh = accFrom.getAmount().getSum().add(new BigDecimal(new BigInteger("10000000")));

        Transaction transaction = new Transaction();
        transaction.setAccountIdFrom(accFrom.getId());
        transaction.setAccountNumberFrom(accFrom.getNumber());
        transaction.setUserFrom(accFrom.getUser().getUsername());
        transaction.setAmount(new Amount("RUR", amountOfTransh));

        Suggestion anotherUser = getSuggestionsToDefaultUser()
                .stream()
                .filter(s -> s.getUserName().equals(userTo.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("no created user for transaction"));

        transaction.setAccountIdTo(anotherUser.getAccountId());
        transaction.setUserTo(anotherUser.getUserName());

        authAdmin().restClientWithoutErrorHandler().postForEntity("/accounts/transaction", transaction, String.class);

        Account accountAdmin = getAccountsOfAdminUser().get(0);
        BigDecimal sumFromAfter = accountAdmin.getAmount().getSum();

        Account accountUser = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToAfter = accountUser.getAmount().getSum();

        assertThat(sumFromBefore, equalTo(sumFromAfter)); //не уменьшилось, хотя сумма даже больше чем есть на счету
        assertThat(sumToBefore.add(amountOfTransh), equalTo(sumToAfter)); //увеличилось
    }

    @Test
    void userCanGetSuggestionsForTransaction() {
        List<Suggestion> suggestionsToDefaultUser = getSuggestionsToDefaultUser();
        List<Account> allAccountsByAdmin = getAllAccountsByAdmin();
        /*Проверить что все suggestion - равны количеству счетов*/
        assertThat(suggestionsToDefaultUser.size(), is(allAccountsByAdmin.size()));
        /*Проверить что счета замаскированы*/
        int count = ((int) suggestionsToDefaultUser
                .stream()
                .filter(s -> s.getMaskAccountNumber().matches("\\*\\*\\* \\d{4}"))
                .count());
        assertThat("Не все счета замаскированы", count, is(suggestionsToDefaultUser.size()));
        /*Проверить что имя юзера который делает запрос заменено на My own account*/
        List<String> my_own_accounts = suggestionsToDefaultUser
                .stream()
                .filter(s -> s.getUserName().equals("My own account"))
                .map(Suggestion::getMaskAccountNumber)
                .collect(Collectors.toList());
        List<String> maskAccounts = getAccountsOfDefaultUser()
                .stream()
                .map(Account::getNumber)
                .map(n -> "*** " + n.split(" ")[3])
                .filter(my_own_accounts::contains)
                .collect(Collectors.toList());
        assertThat(my_own_accounts.size(), is(maskAccounts.size()));
    }


    @Test
    void whenDeletedUserAllHisAccountsDeletedToo() {
        ApplicationUser checkDeletingAccounts = createUser("CheckDeletingAccounts");
        List<Account> accountsOfCreatedUser = getAccountsOfCreatedUser(checkDeletingAccounts.getUsername());
        assertThat("No default account for new user", accountsOfCreatedUser.size(), equalTo(1));
        /*TODO добавить еще один счет для чистоты эксперимента после добавления этой функции*/
        authAdmin().restClientWithErrorHandler()
                .delete("/users?username="+checkDeletingAccounts.getUsername());

        checkUserNotExists(checkDeletingAccounts.getUsername());

        List<Account> allAccountsByAdmin = getAllAccountsByAdmin();
        int sizeAccountsDeletedUsers = allAccountsByAdmin
                .stream()
                .filter(accountsOfCreatedUser::contains)
                .collect(Collectors.toList())
                .size();
        assertThat(sizeAccountsDeletedUsers, equalTo(0));
    }

    private List<Account> getAccountsOfDefaultUser(){
        ResponseEntity<List<Account>> userAccountsEntity = authUser()
                .restClientWithoutErrorHandler().exchange("/accounts/my", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts!=null;
        return accounts;
    }

    private List<Account> getAccountsOfAdminUser(){
        ResponseEntity<List<Account>> userAccountsEntity = authAdmin()
                .restClientWithoutErrorHandler().exchange("/accounts/my", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts!=null;
        return accounts;
    }

    private List<Account> getAllAccountsByAdmin(){
        ResponseEntity<List<Account>> userAccountsEntity = authAdmin()
                .restClientWithoutErrorHandler().exchange("/accounts/all", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
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
