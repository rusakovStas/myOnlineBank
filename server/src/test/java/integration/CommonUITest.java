package integration;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.stasdev.backend.BackendApplication;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.Properties;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = BackendApplication.class)//Так мы запускаем приложение используя тестовое окружение (Junit 5 вклинивается в модель спринг. За счет этого даже BeforeAll выполнится перед стартом спринг приложения). Важно помнить не смешивать данную аннотацию с MockMVC потому что она делает dirtyContext и спринг будет стартовать каждый раз заново для каждого теста
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")// переопределяем проперти для запуска
@TestInstance(TestInstance.Lifecycle.PER_CLASS)//Это необходимо что бы BeforeAll выполнялся после старта спринга (потому что будет выполняться только при создание инстанса тестового класса)
public abstract class CommonUITest {

    private static int appPort = TestProperties.getInstance().getAppPort();

    @BeforeAll
    static void setUp() {
        SelenideLogger.addListener("allure", new AllureSelenide());
        Configuration.baseUrl = Configuration.baseUrl
                .replace(":8080", "")
                .replace("http://","");
        open(String.format("http://%s:%d", Configuration.baseUrl, appPort));
    }

    @AfterAll
    static void tearDown() {
        SelenideLogger.removeListener("allure");
    }


}
