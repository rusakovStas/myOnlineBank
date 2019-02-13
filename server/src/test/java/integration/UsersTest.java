package integration;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class UsersTest extends CommonUITest {

    @BeforeEach
    void login(){
        Selenide.clearBrowserLocalStorage();
        Configuration.timeout = 10_000;
        $("#email").setValue("admin");
        $("#password").setValue("pass");
        $(byText("Login")).click();
        $(byText("Admin")).shouldBe(visible);
    }

    @AfterEach
    void logout(){
        $(byText("logout")).click();
        $(byText("Login")).shouldBe(visible);
    }

    @Test
    void adminCanCreateAndDeleteUser() {
        $(byText("Admin")).click();
        $(byText("Add new user")).click();
        $("#username").setValue("Check creating user");
        $("#password").setValue("pass");
        $(byText("Add")).click();
        $$(".card")
                .findBy(textCaseSensitive("Check creating user"))
                .shouldBe(visible);
        $(byText("Check creating user"))
                .closest("div")
                .find("button").shouldHave(text("Delete"))
                .shouldHave(enabled)
                .click();
        $(byText("Yes")).should(enabled).click();
        $$(".card")
                .findBy(textCaseSensitive("Check creating user"))
                .shouldNotBe(exist);
    }

    @Test
    void adminCanNotDeleteYourself() {
        $(byText("Admin")).click();
        $(byText("admin"))
                .closest("div")
                .find("button").shouldHave(text("Delete"))
                .shouldBe(disabled);
    }

    @Test
    void adminCanNotCreateUserWithNameWhichIsAlreadyExists() {
        $(byText("Admin")).click();
        $(byText("Add new user")).click();
        $("#username").setValue("user");
        $("#password").setValue("pass");
        $(byText("Add")).click();
        $(byText("User with name 'user' already exists!"));
        $(".close").click();

    }

    @Test
    void userCreatedByAdminCanLoginAndNotSeeAdminPage() {
        $(byText("Admin")).click();
        $(byText("Add new user")).click();
        $("#username").setValue("User created by admin");
        $("#password").setValue("pass");
        $(byText("Add")).click();
        $(byText("User created by admin")).shouldBe(visible);
        $(byText("logout")).click();
        $(byText("Login")).shouldBe(visible);
        $("#email").setValue("User created by admin");
        $("#password").setValue("pass");
        $(byText("Login")).click();
        $(byText("Admin")).shouldNotBe(visible);
    }

}
