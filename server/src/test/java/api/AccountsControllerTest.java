package api;

import com.stasdev.backend.errors.NotImplementedYet;
import com.stasdev.backend.model.entitys.*;
import com.stasdev.backend.model.services.impl.PreparerImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;


import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
* Accounts API tests
* Проверяем защищенность всех ендпоинтов
* и тестируем все операции
* TODO придумать удаление юзера после теста - возможно нужно организовать "отмену команды"
* TODO думаю следует вынести все сообщения об ошибках в специальный отдельный класс который можно будет юзать и в тестах
*
* */
class AccountsControllerTest extends CommonApiTest {

    private static final String NOT_ENOUGH_MONEY_ERROR = "On account with number %s amount of money is %s and that not enough for transaction with amount %s";
    private static final String NO_ACCOUNTS_WITH_ID_ERROR = "User '%s' have no account with id %d";
    private static final String DIFFERENT_CURRENCY_ERROR = "Comparison between different currency not implemented yet";
    private static final String YOU_DON_T_HAS_ACCOUNT_WITH_ID = "You don't has account with id %d";
    private static final String USER_HASN_T_ROLE_ADMIN_AND_CAN_T_DO_TRANSACTION_FROM_NOT_HIS_ACCOUNTS = "User '%s' hasn't role admin and can't do transaction from not his accounts";
    private static final String TOPIC_PUSH = "/topic/push/";
    private static final String TOPIC_ACCOUNTS = "/topic/accounts/";

    private String socketURL = "http://localhost:" + port + "/online-bank";


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
        assertThat(accounts.size(), is(3));
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
    @Disabled
    void userCanDoTransaction() throws InterruptedException, ExecutionException, TimeoutException {

        StompSession stompSession = getStompClient()
                .connect(socketURL, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

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

        StompHandler<Account> accountStompHandlerFrom = new StompHandler<>(1, () -> Account.class);
        StompHandler<Account> accountStompHandlerTo = new StompHandler<>(1, () -> Account.class);
        StompHandler<Push> pushStompHandlerFrom = new StompHandler<>(1, () -> Push.class);
        StompHandler<Push> pushStompHandlerTo = new StompHandler<>(1, () -> Push.class);

        stompSession.subscribe(TOPIC_PUSH + transaction.getUserFrom(), pushStompHandlerFrom);
        stompSession.subscribe(TOPIC_PUSH + transaction.getUserTo(), pushStompHandlerTo);
        stompSession.subscribe(TOPIC_ACCOUNTS + transaction.getUserFrom(), accountStompHandlerFrom);
        stompSession.subscribe(TOPIC_ACCOUNTS + transaction.getUserTo(), accountStompHandlerTo);

        authUser().restClientWithoutErrorHandler().postForEntity("/accounts/transaction", transaction, String.class);

        Account accountDef = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromAfter = accountDef.getAmount().getSum();

        Account accountUser = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToAfter = accountUser.getAmount().getSum();

        assertThat(sumFromBefore.subtract(amountOfTransh), equalTo(sumFromAfter));
        assertThat(sumToBefore.add(amountOfTransh), equalTo(sumToAfter));

        Account accountUpdatedFrom = accountStompHandlerFrom
                .getMessage(0)
                .get(3, SECONDS);
        Account accountUpdatedTo = accountStompHandlerTo
                .getMessage(0)
                .get(3, SECONDS);
        Push pushFromCheck = pushStompHandlerFrom
                .getMessage(0)
                .get(3, SECONDS);
        Push pushToCheck = pushStompHandlerTo
                .getMessage(0)
                .get(3, SECONDS);
        assertThat(accountDef, is(equalTo(accountUpdatedFrom)));
        assertThat(accountUser, is(equalTo(accountUpdatedTo)));

        assertThat(accountUpdatedFrom.getName(), is(accountDef.getName()));
        assertThat(accountUpdatedFrom.getNumber(), isEmptyOrNullString());
        assertThat(accountUpdatedFrom.getUser(), is(nullValue()));

        assertThat(accountUpdatedTo.getName(), is(accountUser.getName()));
        assertThat(accountUpdatedTo.getNumber(), isEmptyOrNullString());
        assertThat(accountUpdatedTo.getUser(), is(nullValue()));

        assertThat(pushFromCheck.getMsg(), equalTo("Your transaction was successful"));
        assertThat(pushToCheck.getMsg(), equalTo(String.format("User '%s' sent money for you", transaction.getUserFrom())));
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
        ApplicationUser userTo = createUser("newNotDefaultUser");
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
    void userCanNotDoTransactionFromNotExistingAccount() {
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
    @Disabled
    void adminCanDoTransactionFromAnyAccounts() throws InterruptedException, ExecutionException, TimeoutException {

        StompSession stompSession = getStompClient()
                .connect(socketURL, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

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

        StompHandler<Account> accountStompHandlerFrom = new StompHandler<>(1, () -> Account.class);
        StompHandler<Account> accountStompHandlerTo = new StompHandler<>(1, () -> Account.class);
        StompHandler<Account> accountStompHandlerAdmin = new StompHandler<>(2, () -> Account.class);
        StompHandler<Push> pushStompHandlerAdmin = new StompHandler<>(1, () -> Push.class);
        StompHandler<Push> pushStompHandlerFrom = new StompHandler<>(1, () -> Push.class);
        StompHandler<Push> pushStompHandlerTo = new StompHandler<>(1, () -> Push.class);

        stompSession.subscribe(TOPIC_PUSH + transaction.getUserFrom(), pushStompHandlerFrom);
        stompSession.subscribe(TOPIC_PUSH + transaction.getUserTo(), pushStompHandlerTo);
        stompSession.subscribe(TOPIC_ACCOUNTS + transaction.getUserFrom(), accountStompHandlerFrom);
        stompSession.subscribe(TOPIC_ACCOUNTS + transaction.getUserTo(), accountStompHandlerTo);

        String adminName = "admin";
        stompSession.subscribe(TOPIC_ACCOUNTS + adminName, accountStompHandlerAdmin);
        stompSession.subscribe(TOPIC_PUSH + adminName, pushStompHandlerAdmin);


        authAdmin().restClientWithoutErrorHandler().postForEntity("/accounts/transaction", transaction, String.class);

        Account accountDef = getAccountsOfDefaultUser().get(0);
        BigDecimal sumFromAfter = accountDef.getAmount().getSum();

        Account accountUser = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToAfter = accountUser.getAmount().getSum();

        assertThat(sumFromBefore.subtract(amountOfTransh), equalTo(sumFromAfter));
        assertThat(sumToBefore.add(amountOfTransh), equalTo(sumToAfter));

        Account accountUpdatedFrom = accountStompHandlerFrom
                .getMessage(0)
                .get(3, SECONDS);
        Account accountUpdatedTo = accountStompHandlerTo
                .getMessage(0)
                .get(3, SECONDS);

        Account accountUpdatedFromForAdmin = accountStompHandlerAdmin
                .getMessage(0)
                .get(3, SECONDS);
        Account accountUpdatedToForAdmin = accountStompHandlerAdmin
                .getMessage(1)
                .get(3, SECONDS);
        Push pushAdminCheck = pushStompHandlerAdmin
                .getMessage(0)
                .get(3, SECONDS);

        Push pushFromCheck = pushStompHandlerFrom
                .getMessage(0)
                .get(3, SECONDS);
        Push pushToCheck = pushStompHandlerTo
                .getMessage(0)
                .get(3, SECONDS);

        assertThat(accountDef, is(equalTo(accountUpdatedFrom)));
        assertThat(accountUser, is(equalTo(accountUpdatedTo)));

        assertThat(accountUpdatedFromForAdmin, not(equalTo(accountUpdatedToForAdmin))); // Проверяем что пришло два разных счета
//      Проверяем что это те счета которые участвовали в транзакции
        assertThat(accountDef, isOneOf(accountUpdatedFromForAdmin, accountUpdatedToForAdmin));
        assertThat(accountUser, isOneOf(accountUpdatedToForAdmin, accountUpdatedFromForAdmin));

        assertThat(accountUpdatedFrom.getName(), is(accountDef.getName()));
        assertThat(accountUpdatedFrom.getNumber(), isEmptyOrNullString());
        assertThat(accountUpdatedFrom.getUser(), is(nullValue()));

//      Проверяем что вся чувствительная информация была убрана из сообщений от сокета
        assertThat(accountUpdatedTo.getName(), is(accountUser.getName()));
        assertThat(accountUpdatedTo.getNumber(), isEmptyOrNullString());
        assertThat(accountUpdatedTo.getUser(), is(nullValue()));

        assertThat(accountUpdatedFromForAdmin.getName(), isOneOf(accountDef.getName(),accountUser.getName()));
        assertThat(accountUpdatedFromForAdmin.getNumber(), isEmptyOrNullString());
        assertThat(accountUpdatedFromForAdmin.getUser(), is(nullValue()));

        assertThat(accountUpdatedToForAdmin.getName(), isOneOf(accountUser.getName(), accountDef.getName()));
        assertThat(accountUpdatedToForAdmin.getNumber(), isEmptyOrNullString());
        assertThat(accountUpdatedToForAdmin.getUser(), is(nullValue()));

        assertThat(pushAdminCheck.getMsg(), equalTo("Your transaction was successful"));
        assertThat(pushFromCheck.getMsg(), equalTo("Your transaction was successful"));
        assertThat(pushToCheck.getMsg(), equalTo(String.format("User '%s' sent money for you", transaction.getUserFrom())));
    }

    @Test
    void adminCanSendMoneyToHimselfByAnotherUser() {
        BigDecimal amountOfTransh = new BigDecimal(new BigInteger("10"));
        Account firstAccountOfDefaultUser = getAccountsOfDefaultUser().get(0);
        Account firstAccountOfAdmin = getAccountsOfAdminUser().get(0);

        Transaction transaction = new Transaction();
        transaction.setAccountIdFrom(firstAccountOfDefaultUser.getId());
        transaction.setAccountNumberFrom(firstAccountOfDefaultUser.getNumber());
        transaction.setAmount(new Amount("RUR", amountOfTransh));
        transaction.setUserFrom(firstAccountOfDefaultUser.getUser().getUsername());

        transaction.setAccountIdTo(firstAccountOfAdmin.getId());
        transaction.setAccountNumberTo(firstAccountOfAdmin.getNumber());
        transaction.setUserTo(firstAccountOfAdmin.getUser().getUsername());

        authAdmin().restClientWithoutErrorHandler().postForEntity("/accounts/transaction", transaction, String.class);

        Account afterFrom = getAccountsOfDefaultUser().get(0);
        Account afterTo = getAccountsOfAdminUser().get(0);

        assertThat(firstAccountOfDefaultUser.getAmount().getSum().subtract(amountOfTransh), equalTo(afterFrom.getAmount().getSum()));
        /*TODO подумать над проверкой суммы админа - смысла в ней нет*/
        assertThat(firstAccountOfAdmin.getAmount().getSum().add(amountOfTransh), equalTo(afterTo.getAmount().getSum()));
    }

    @Test
    @Disabled
    void adminCanDoTransactionFromHisOwnAccounts() throws InterruptedException, ExecutionException, TimeoutException {
        StompSession stompSession = getStompClient()
                .connect(socketURL, new StompSessionHandlerAdapter() {}).get(1, SECONDS);

        ApplicationUser userTo = createUser("userToWhichNotAdminAgain");
        Account accFrom = getAccountsOfAdminUser().get(0); // Транзакцию выполняем со счета админа
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

        StompHandler<Account> accountStompHandlerFrom = new StompHandler<>(1, () -> Account.class);
        StompHandler<Account> accountStompHandlerTo = new StompHandler<>(1, () -> Account.class);
        StompHandler<Account> accountStompHandlerAdmin = new StompHandler<>(2, () -> Account.class);
        StompHandler<Push> pushStompHandlerAdmin = new StompHandler<>(1, () -> Push.class);
        StompHandler<Push> pushStompHandlerFrom = new StompHandler<>(1, () -> Push.class);
        StompHandler<Push> pushStompHandlerTo = new StompHandler<>(1, () -> Push.class);

        stompSession.subscribe(TOPIC_PUSH + transaction.getUserFrom(), pushStompHandlerFrom);
        stompSession.subscribe(TOPIC_PUSH + transaction.getUserTo(), pushStompHandlerTo);
        stompSession.subscribe(TOPIC_ACCOUNTS + transaction.getUserFrom(), accountStompHandlerFrom);
        stompSession.subscribe(TOPIC_ACCOUNTS + transaction.getUserTo(), accountStompHandlerTo);

        String adminName = "admin";
        stompSession.subscribe(TOPIC_ACCOUNTS + adminName, accountStompHandlerAdmin);
        stompSession.subscribe(TOPIC_PUSH + adminName, pushStompHandlerAdmin);


        authAdmin().restClientWithoutErrorHandler().postForEntity("/accounts/transaction", transaction, String.class);

        Account accountDef = getAccountsOfAdminUser().get(0);
        BigDecimal sumFromAfter = accountDef.getAmount().getSum();

        Account accountUser = getAccountsOfCreatedUser(userTo.getUsername()).get(0);
        BigDecimal sumToAfter = accountUser.getAmount().getSum();

        assertThat(sumFromBefore, equalTo(sumFromAfter)); // Не уменьшается так как счет админа
        assertThat(sumToBefore.add(amountOfTransh), equalTo(sumToAfter));

        Account accountUpdatedFrom = accountStompHandlerFrom
                .getMessage(0)
                .get(3, SECONDS);
        Account accountUpdatedTo = accountStompHandlerTo
                .getMessage(0)
                .get(3, SECONDS);

        Account accountUpdatedFromForAdmin = accountStompHandlerAdmin
                .getMessage(0)
                .get(3, SECONDS);
        Account accountUpdatedToForAdmin = accountStompHandlerAdmin
                .getMessage(1)
                .get(3, SECONDS);
        Push pushAdminCheck = pushStompHandlerAdmin
                .getMessage(0)
                .get(3, SECONDS);
        Push pushFromCheck = pushStompHandlerFrom
                .getMessage(0)
                .get(3, SECONDS);
        Push pushToCheck = pushStompHandlerTo
                .getMessage(0)
                .get(3, SECONDS);

        assertThat(accountDef, is(equalTo(accountUpdatedFrom)));
        assertThat(accountUser, is(equalTo(accountUpdatedTo)));

        assertThat(accountUpdatedFromForAdmin, not(equalTo(accountUpdatedToForAdmin))); // Проверяем что пришло два разных счета
//      Проверяем что это те счета которые участвовали в транзакции
        assertThat(accountDef, isOneOf(accountUpdatedFromForAdmin, accountUpdatedToForAdmin));
        assertThat(accountUser, isOneOf(accountUpdatedToForAdmin, accountUpdatedFromForAdmin));

        assertThat(accountUpdatedFrom.getName(), is(accountDef.getName()));
        assertThat(accountUpdatedFrom.getNumber(), isEmptyOrNullString());
        assertThat(accountUpdatedFrom.getUser(), is(nullValue()));

//      Проверяем что вся чувствительная информация была убрана из сообщений от сокета
        assertThat(accountUpdatedTo.getName(), is(accountUser.getName()));
        assertThat(accountUpdatedTo.getNumber(), isEmptyOrNullString());
        assertThat(accountUpdatedTo.getUser(), is(nullValue()));

        assertThat(accountUpdatedFromForAdmin.getName(), isOneOf(accountDef.getName(),accountUser.getName()));
        assertThat(accountUpdatedFromForAdmin.getNumber(), isEmptyOrNullString());
        assertThat(accountUpdatedFromForAdmin.getUser(), is(nullValue()));

        assertThat(accountUpdatedToForAdmin.getName(), isOneOf(accountUser.getName(), accountDef.getName()));
        assertThat(accountUpdatedToForAdmin.getNumber(), isEmptyOrNullString());
        assertThat(accountUpdatedToForAdmin.getUser(), is(nullValue()));

        assertThat(pushAdminCheck.getMsg(), equalTo("Your transaction was successful"));
        assertThat(pushFromCheck.getMsg(), equalTo("Your transaction was successful"));
        assertThat(pushToCheck.getMsg(), equalTo(String.format("User '%s' sent money for you", transaction.getUserFrom())));
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
    void userCanNotSendTransactionWithNegativeAmount() {
        BigDecimal amountOfTransh = new BigDecimal(new BigInteger("-10"));
        Account firstAccountOfDefaultUser = getAccountsOfDefaultUser().get(0);
        Account firstAccountOfAdmin = getAccountsOfAdminUser().get(0);

        Transaction transaction = new Transaction();
        transaction.setAccountIdFrom(firstAccountOfDefaultUser.getId());
        transaction.setAccountNumberFrom(firstAccountOfDefaultUser.getNumber());
        transaction.setAmount(new Amount("RUR", amountOfTransh));
        transaction.setUserFrom(firstAccountOfDefaultUser.getUser().getUsername());

        transaction.setAccountIdTo(firstAccountOfAdmin.getId());
        transaction.setAccountNumberTo(firstAccountOfAdmin.getNumber());
        transaction.setUserTo(firstAccountOfAdmin.getUser().getUsername());

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authUser()
                        .restClientWithErrorHandler().postForEntity("/accounts/transaction", transaction, String.class));
        assertThat(runtimeException.getMessage(), containsString("You can't do transaction with negative amount"));
    }

    @Test
    void userCanNotSendTransactionWithAmountWithScaleMoreThenTwo() {
        BigDecimal amountOfTransh = new BigDecimal("10.111");
        Account firstAccountOfDefaultUser = getAccountsOfDefaultUser().get(0);
        Account firstAccountOfAdmin = getAccountsOfAdminUser().get(0);

        Transaction transaction = new Transaction();
        transaction.setAccountIdFrom(firstAccountOfDefaultUser.getId());
        transaction.setAccountNumberFrom(firstAccountOfDefaultUser.getNumber());
        transaction.setAmount(new Amount("RUR", amountOfTransh));
        transaction.setUserFrom(firstAccountOfDefaultUser.getUser().getUsername());

        transaction.setAccountIdTo(firstAccountOfAdmin.getId());
        transaction.setAccountNumberTo(firstAccountOfAdmin.getNumber());
        transaction.setUserTo(firstAccountOfAdmin.getUser().getUsername());

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authUser()
                        .restClientWithErrorHandler().postForEntity("/accounts/transaction", transaction, String.class));
        assertThat(runtimeException.getMessage(), containsString("Amount of transaction must to have scale no more than 2"));
    }

    @Test
    void userCanNotDeleteAccountsAnotherUsers() {
        ApplicationUser userForCheckAccountDeleting = createUser("UserForCheckForbiddenDeletingAccAnotherUser");
        List<Account> accountsOfDefaultUser = getAccountsOfDefaultUser();
        Account account = accountsOfDefaultUser.get(0);
        int allAccSizeBefore = getAllAccountsByAdmin().size();
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authByUser(userForCheckAccountDeleting.getUsername(), DEFAULT_PASSWORD)
                        .restClientWithErrorHandler().delete("/accounts?id=" + account.getId()));
        assertThat(runtimeException.getMessage(), containsString(String.format(YOU_DON_T_HAS_ACCOUNT_WITH_ID, account.getId())));
        int allAccSizeAfter = getAllAccountsByAdmin().size();
        assertThat(allAccSizeBefore, is(allAccSizeAfter));
    }

    @Test
    void adminCanNotDeleteAnyAccount() {
        ApplicationUser userForCheckAccountDeleting = createUser("UserForCheckAccountDeletingByAdmin");
        List<Account> accountsOfCreatedUser = getAccountsOfCreatedUser(userForCheckAccountDeleting.getUsername());
        int allAccSizeBefore = getAllAccountsByAdmin().size();
        Account account = accountsOfCreatedUser.get(0);
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> authAdmin()
                        .restClientWithErrorHandler().delete("/accounts?id=" + account.getId()));
        assertThat(runtimeException.getMessage(), containsString(String.format(YOU_DON_T_HAS_ACCOUNT_WITH_ID, account.getId())));
        int allAccSizeAfter = getAllAccountsByAdmin().size();
        assertThat(allAccSizeBefore, is(allAccSizeAfter));
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
    void userCanGetSuggestionsWithExcludeAccount() {
        List<Account> allAccountsByAdmin = getAllAccountsByAdmin();
        List<Account> accountsOfDefaultUser = getAccountsOfDefaultUser();
        Long excludeId = accountsOfDefaultUser.get(0).getId();
        List<Suggestion> suggestionsToDefaultUser = getSuggestionsToDefaultUserWithExclude(excludeId);
        /*Проверить что все suggestion - равны количеству счетов минус тот который решили отфильтровать*/
        assertThat(suggestionsToDefaultUser.size(), is(allAccountsByAdmin.size() - 1));
        /*Проверить что среди suggestion - нет счета с id который мы исключили*/
        assertFalse(suggestionsToDefaultUser.stream().anyMatch(s -> s.getAccountId().equals(excludeId)));

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

    @Test
    void userCanCreateNewAccount() {
        Account accountForCreation = new Account();
        String userName = "userForCheckCreationOfAccount";
        ApplicationUser userForCheckCreationOfAccount = createUser(userName);
        accountForCreation.setUser(userForCheckCreationOfAccount);
        List<Account> accountsOfCreatedUserBeforeCreation = getAccountsOfCreatedUser(userName);
        int sizeBeforeCreation = accountsOfCreatedUserBeforeCreation.size();


        ResponseEntity<Account> createdAccountAsResponse = authUser().restClientWithoutErrorHandler().postForEntity("/accounts", accountForCreation, Account.class);

        List<Account> accountsOfCreatedUserAfterCreation = getAccountsOfCreatedUser(userName);
        int sizeAfterCreation = accountsOfCreatedUserAfterCreation.size();
        assertThat(sizeAfterCreation, is(sizeBeforeCreation + 1));
        List<Account> createdAccount = accountsOfCreatedUserAfterCreation
                .stream()
                .filter(a -> !accountsOfCreatedUserBeforeCreation.contains(a))
                .collect(Collectors.toList());
        assertThat(createdAccount.size(), is(1));
        Account account = createdAccount.get(0);
        assertThat(account, is(createdAccountAsResponse.getBody()));
        assertThat(account.getUser().getUsername(), is(userForCheckCreationOfAccount.getUsername()));
        BigDecimal expected = new BigDecimal(new BigInteger("0"));
        expected = expected.setScale(2);
        assertThat(account.getAmount().getSum(), is(expected));
        assertThat(account.getAmount().getCurrency(), is("RUR"));
        assertThat(account.getName(), isEmptyOrNullString());
        assertThat(account.getNumber().matches(String.format("%s \\d{4} \\d{4}", PreparerImpl.prefixOfAccountNumber)), is(true));
        assertThat(account.getId(), notNullValue());
    }


    @Test
    void adminCanCreateAccount() {
        Account accountForCreation = new Account();
        ApplicationUser adminUser = new ApplicationUser();
        adminUser.setUsername("admin");
        accountForCreation.setUser(adminUser);
        List<Account> accountsOfAdminUserBeforeCreation = getAccountsOfAdminUser();
        int sizeBeforeCreation = accountsOfAdminUserBeforeCreation.size();


        ResponseEntity<Account> createdAccountAsResponse = authAdmin().restClientWithoutErrorHandler().postForEntity("/accounts", accountForCreation, Account.class);

        List<Account> accountsOfAdminUserAfterCreation = getAccountsOfAdminUser();
        int sizeAfterCreation = accountsOfAdminUserAfterCreation.size();
        assertThat(sizeAfterCreation, is(sizeBeforeCreation + 1));
        List<Account> createdAccount = accountsOfAdminUserAfterCreation
                .stream()
                .filter(a -> !accountsOfAdminUserBeforeCreation.contains(a))
                .collect(Collectors.toList());
        assertThat(createdAccount.size(), is(1));
        Account account = createdAccount.get(0);
        assertThat(account, is(createdAccountAsResponse.getBody()));
        assertThat(account.getUser().getUsername(), is(adminUser.getUsername()));
        BigDecimal expected = new BigDecimal(new BigInteger("0"));
        expected = expected.setScale(2);
        assertThat(account.getAmount().getSum(), is(expected));
        assertThat(account.getAmount().getCurrency(), is("RUR"));
        assertThat(account.getName(), isEmptyOrNullString());
        assertThat(account.getNumber().matches(String.format("%s \\d{4} \\d{4}", PreparerImpl.prefixOfAccountNumber)), is(true));
        assertThat(account.getId(), notNullValue());
    }


    private WebSocketStompClient getStompClient() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());//Если этого не будет он будет МОЛЧА падать при попытке кастануть payload к нужному типу
        return stompClient;
    }


    private class StompHandler<T> implements StompFrameHandler {

        private List<CompletableFuture<T>> completableFutureList;
        private Supplier<Type> typeSupplier;

        StompHandler(int expectedMessageSize, Supplier<Type> typeSupplier) {
            this.typeSupplier = typeSupplier;
            this.completableFutureList = new ArrayList<>(expectedMessageSize);
            for (int i = 0; i < expectedMessageSize; i++) {
                this.completableFutureList.add(new CompletableFuture<>());
            }
        }

        public CompletableFuture<T> getMessage(int i){
            return this.completableFutureList.get(i);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            return typeSupplier.get();
        }

        @Override
        public synchronized void handleFrame(StompHeaders headers, Object payload) {
            this.completableFutureList.stream()
                    .filter(c -> !c.isDone())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unexpected message "+ (T) payload))
                    .complete((T) payload);
        }
    }

    private List<Transport> createTransportClient() {
        return Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
    }


}
