package api;

import com.stasdev.backend.BackendApplication;
import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import common.ApiFunctions;
import common.TestProperties;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = BackendApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")// переопределяем проперти для запуска
@TestInstance(TestInstance.Lifecycle.PER_CLASS)//Это необходимо что бы BeforeAll выполнялся после старта спринга (потому что будет выполняться только при создание инстанса тестового класса)
abstract class CommonApiTest extends ApiFunctions{


}
