package common;

import com.stasdev.backend.model.entitys.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/*TODO возможно лучше через делегата?*/
public abstract class ApiFunctions {

    protected int port = TestProperties.getInstance().getAppPort();

    protected static final String DEFAULT_PASSWORD = "Password";
    @Autowired
    private TestRestTemplate restClient;

    protected void clear(){
        restClient.getRestTemplate().getInterceptors().clear();
        //Устанавливаем "пустой" обработчик ошибок
        restClient.getRestTemplate().setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
    }

    protected AccessToRestClient authByUser(String username, String password){
        clear();
        restClient.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("Authorization", "Basic Y2xpZW50LWlkOnNlY3JldA==");
                    return execution.execute(request, body);
                }));
        ResponseEntity<Map> token = restClient.postForEntity(String.format("/oauth/token?grant_type=password&username=%s&password=%s", username, password), null, Map.class);
        Map tokenBody = token.getBody();
        assert tokenBody != null;
        String access_token = tokenBody.getOrDefault("access_token", "no token").toString();
        assertThat(access_token, not(equalTo("no token")));
        restClient.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("Authorization", "Bearer "+ access_token);
                    return execution.execute(request, body);
                }));
        return new AccessToRestClient(restClient);
    }

    protected AccessToRestClient authAdmin(){
        return authByUser("admin", "pass");
    }

    protected AccessToRestClient authUser(){
        return authByUser("user", "pass");
    }

    protected AccessToRestClient nonAuth(){
        clear();
        return new AccessToRestClient(restClient);
    }


    protected class AccessToRestClient{
        private TestRestTemplate testRestTemplate;

        private AccessToRestClient(TestRestTemplate  template){
            this.testRestTemplate = template;
        }

        public TestRestTemplate restClientWithoutErrorHandler() {
            return testRestTemplate;
        }

        public TestRestTemplate restClientWithErrorHandler(){
            restClient.getRestTemplate().setErrorHandler(new ResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse response) throws IOException {
                    return response.getStatusCode().isError();
                }

                @Override
                public void handleError(ClientHttpResponse response) throws IOException {
                    StringBuilder textBuilder = new StringBuilder();
                    try (Reader reader = new BufferedReader(new InputStreamReader
                            (response.getBody(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                        int c = 0;
                        while ((c = reader.read()) != -1) {
                            textBuilder.append((char) c);
                        }
                    }
                    throw new RuntimeException(textBuilder.toString());
                }
            });
            return testRestTemplate;
        }
    }

    protected void createUserByUser(String createdUser){
        authUser()
                .restClientWithErrorHandler()
                .postForEntity("/users", new ApplicationUser(createdUser, DEFAULT_PASSWORD), ApplicationUser.class);
    }

    protected ResponseEntity<ApplicationUser> createUserByAdmin(String userName){
        return authAdmin()
                .restClientWithErrorHandler()
                .postForEntity("/users", new ApplicationUser(userName, DEFAULT_PASSWORD), ApplicationUser.class);
    }

    protected void checkUserExists(String userName){
        ResponseEntity<List<ApplicationUser>> allUserRs = authAdmin().restClientWithoutErrorHandler()
                .exchange("/users/all", HttpMethod.GET,null, new ParameterizedTypeReference<List<ApplicationUser>>(){} );
        List<ApplicationUser> allUsers = allUserRs.getBody();
        assert allUsers != null;
        assertThat(allUsers.stream().anyMatch(u -> u.getUsername().equals(userName)), is(true));
    }

    protected void checkUserNotExists(String userName){
        ResponseEntity<List<ApplicationUser>> allUserRs = authAdmin().restClientWithoutErrorHandler()
                .exchange("/users/all",HttpMethod.GET,null, new ParameterizedTypeReference<List<ApplicationUser>>(){} );
        List<ApplicationUser> allUsers = allUserRs.getBody();
        assert allUsers != null;
        assertThat(allUsers.stream().anyMatch(u -> u.getUsername().equals(userName)), is(false));
    }

    protected void doTransactionByCreatedUserToFirstAccount(String fromCreatedUser, String toUser, String amount){
        Transaction transaction = new Transaction();
        transaction.setUserFrom(fromCreatedUser);
        transaction.setUserTo(toUser);
        Account accountFrom = getAllAccountsByAdmin()
                .stream()
                .filter(a -> a.getUser().getUsername().equals(fromCreatedUser))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User " + fromCreatedUser + " don't have accounts"));
        Account accountTo = getAllAccountsByAdmin()
                .stream()
                .filter(a -> a.getUser().getUsername().equals(toUser))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User " + toUser + " don't have accounts"));
        transaction.setAccountIdFrom(accountFrom.getId());
        transaction.setAccountIdTo(accountTo.getId());
        transaction.setAmount(new Amount("RUR", new BigDecimal(amount)));
        authByUser(fromCreatedUser, DEFAULT_PASSWORD)
                .restClientWithoutErrorHandler()
                .postForEntity("/accounts/transaction", transaction, String.class);
    }

    protected void doTransactionByAdminToFirstAccount(String fromCreatedUser, String toUser, String amount){
        Transaction transaction = new Transaction();
        transaction.setUserFrom(fromCreatedUser);
        transaction.setUserTo(toUser);
        Account accountFrom = getAllAccountsByAdmin()
                .stream()
                .filter(a -> a.getUser().getUsername().equals(fromCreatedUser))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User " + fromCreatedUser + " don't have accounts"));
        Account accountTo = getAllAccountsByAdmin()
                .stream()
                .filter(a -> a.getUser().getUsername().equals(toUser))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User " + toUser + " don't have accounts"));
        transaction.setAccountIdFrom(accountFrom.getId());
        transaction.setAccountIdTo(accountTo.getId());
        transaction.setAmount(new Amount("RUR", new BigDecimal(amount)));
        authAdmin()
                .restClientWithoutErrorHandler()
                .postForEntity("/accounts/transaction", transaction, String.class);
    }

    protected List<Account> getAccountsOfDefaultUser(){
        ResponseEntity<List<Account>> userAccountsEntity = authUser()
                .restClientWithoutErrorHandler().exchange("/accounts/my", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts!=null;
        return accounts;
    }

    protected List<Account> getAllAccountsByAdmin(){
        ResponseEntity<List<Account>> userAccountsEntity = authAdmin()
                .restClientWithoutErrorHandler().exchange("/accounts/all", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts!=null;
        return accounts;
    }

    protected List<Account> getAccountsOfCreatedUser(String user){
        ResponseEntity<List<Account>> userAccountsEntity = authByUser(user, DEFAULT_PASSWORD)
                .restClientWithoutErrorHandler().exchange("/accounts/my", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts!=null;
        return accounts;
    }

    protected List<Suggestion> getSuggestionsToDefaultUser(){
        ResponseEntity<List<Suggestion>> suggestionsEntity = authUser()
                .restClientWithoutErrorHandler()
                .exchange("/accounts/suggestions", HttpMethod.GET,null, new ParameterizedTypeReference<List<Suggestion>>(){});

        List<Suggestion> suggestions = suggestionsEntity.getBody();
        assert suggestions != null;
        return suggestions;
    }

    protected List<Suggestion> getSuggestionsToDefaultUserWithExclude(Long excludeId){
        ResponseEntity<List<Suggestion>> suggestionsEntity = authUser()
                .restClientWithoutErrorHandler()
                .exchange("/accounts/suggestions?excludeAccountId=" + excludeId, HttpMethod.GET,null, new ParameterizedTypeReference<List<Suggestion>>(){});

        List<Suggestion> suggestions = suggestionsEntity.getBody();
        assert suggestions != null;
        return suggestions;
    }

    protected List<Suggestion> getSuggestionsToCreatedUser(String user){
        ResponseEntity<List<Suggestion>> suggestionsEntity = authByUser(user,DEFAULT_PASSWORD)
                .restClientWithoutErrorHandler()
                .exchange("/accounts/suggestions", HttpMethod.GET,null, new ParameterizedTypeReference<List<Suggestion>>(){});

        List<Suggestion> suggestions = suggestionsEntity.getBody();
        assert suggestions != null;
        return suggestions;
    }

    protected ApplicationUser createUser(String userName){
        ResponseEntity<ApplicationUser> userByAdmin = createUserByAdmin(userName);
        ApplicationUser body = userByAdmin.getBody();
        assert body!=null;
        return body;
    }

    protected List<Account> getAccountsOfAdminUser(){
        ResponseEntity<List<Account>> userAccountsEntity = authAdmin()
                .restClientWithoutErrorHandler().exchange("/accounts/my", HttpMethod.GET, null, new ParameterizedTypeReference<List<Account>>() {});
        List<Account> accounts = userAccountsEntity.getBody();
        assert accounts!=null;
        return accounts;
    }

}
