package api;

import com.stasdev.backend.BackendApplication;
import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/*
* Главный класс от которого необходимо наследовать все остальные классы для тестирования
* Он:
* Запускает приложение с тестовым профайлом и настройками (рандомный порт, база H2 создается с нуля)
* Предоставляет доступ к рест темплейту и основным методам его настройки (за счет этого можно не переживать за то что настройки прошлого теста повлияют на следующие)
* */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BackendApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")// переопределяем проперти для запуска
@TestInstance(TestInstance.Lifecycle.PER_CLASS)//Это необходимо что бы BeforeAll выполнялся после старта спринга (потому что будет выполняться только при создание инстанса тестового класса)
abstract class CommonApiTest {

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


}
