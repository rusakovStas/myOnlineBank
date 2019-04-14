package integration;

import com.codeborne.selenide.*;
import com.stasdev.backend.errors.NotImplementedYet;
import common.TestProperties;
import integration.po.Account;
import integration.po.AccountsPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static org.hamcrest.MatcherAssert.assertThat;


public class AccountTests extends CommonUITest {

    void login(String userName, String pass) {
        int appPort = TestProperties.getInstance().getAppPort();
        Configuration.baseUrl = Configuration.baseUrl
                .replace(":8080", "")
                .replace("http://", "");
//      Эта команда откроет только один браузер в одном потоке, т.е. переоткрываться не будет при каждом тесте
        open(String.format("http://%s:%d", Configuration.baseUrl, appPort));

        Selenide.clearBrowserLocalStorage();
        Configuration.timeout = 10_000;

        $("#email").setValue(userName);
        $("#password").setValue(pass);
        $(byText("Login")).click();
        $(byText("Home")).shouldBe(visible);
    }

    @Test
    void userCanDeleteHisAccount() {
        String defaultUser = "user";
        login(defaultUser, "pass");
        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();
        accountsPage.accountsShouldHave(size(3));

        accountsPage
                .getAccountWithIndex(0)
                .beginDeleteAccount()
                .checkWarningMessage()
                .execute();

        accountsPage.accountsShouldHave(size(2));
    }

    @Test
    void userCanDoTransactionToAnotherUser() {
        String defaultUser = "user";
        String anotherUserName = "anotherUser";
        String amountOfTransaction = "100";

        createUserByAdmin(anotherUserName); //То что созданный юзер имеет дефолтный счет проверялось на апи тестах
        String anotherUserAccountNumber = getAccountsOfCreatedUser(anotherUserName).get(0).getNumber();

        login(defaultUser, "pass");
        $(byText("Accounts")).click();

        AccountsPage accountsPage = new AccountsPage();
        Account firstAccount = accountsPage.getAccountWithIndex(0);
        String moneyInTheAmountBeforeTransaction = firstAccount.getMoneyAmount();
        String expectedMoneyInTheAccountAfterTransaction = new BigDecimal(moneyInTheAmountBeforeTransaction)
                .subtract(new BigDecimal(amountOfTransaction))
                .toPlainString();

        firstAccount
                .beginTransaction()
                .chooseAccountToFromSuggestions(anotherUserName, anotherUserAccountNumber)
                .setAmountOfTransaction(amountOfTransaction)
                .execute();

        firstAccount.accountMoneyShouldHave(text(expectedMoneyInTheAccountAfterTransaction));
        accountsPage.checkPushAboutYourTransactionAndCloseThem();
    }

    @Test
    void userCanDoTransactionOnHisOwnAccount() {
        String defaultUser = "user";
        String amountOfTransaction = "454";

        login(defaultUser, "pass");

        $(byText("Accounts")).click();

        AccountsPage accountsPage = new AccountsPage();
        Account accountFrom = accountsPage.getAccountWithIndex(1);
        Account accountTo = accountsPage.getAccountWithIndex(0);

        String moneyAccountFromBeforeTransaction = accountFrom.getMoneyAmount();
        String moneyAccountToBeforeTransaction = accountTo.getMoneyAmount();

        String expectedMoneyInTheAccountFromAfterTransaction = new BigDecimal(moneyAccountFromBeforeTransaction)
                .subtract(new BigDecimal(amountOfTransaction))
                .toPlainString();

        String expectedMoneyInTheAccountToAfterTransaction = new BigDecimal(moneyAccountToBeforeTransaction)
                .add(new BigDecimal(amountOfTransaction))
                .toPlainString();

        accountFrom
                .beginTransaction()
                .chooseAccountToFromSuggestions(accountTo)
                .setAmountOfTransaction(amountOfTransaction)
                .execute();

        accountTo.accountMoneyShouldHave(text(expectedMoneyInTheAccountToAfterTransaction));
        accountFrom.accountMoneyShouldHave(text(expectedMoneyInTheAccountFromAfterTransaction));
        accountsPage.checkPushAboutTransactionToOwnAccountAndCloseThem(defaultUser);
    }

    @Test
    void userIsReceivingTheMessageAboutTransactionToHim() {
        String defaultUser = "user";
        String anotherUserName = "anotherUserForCheckPush";
        String amountOfTransaction = "100";

        createUserByAdmin(anotherUserName); //То что созданный юзер имеет дефолтный счет проверялось на апи тестах
        login(defaultUser, "pass");

        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();
        Account firstAccount = accountsPage.getAccountWithIndex(0);
        String moneyAccountToBeforeTransaction = firstAccount.getMoneyAmount();
        String expectedMoneyInTheAccountToAfterTransaction = new BigDecimal(moneyAccountToBeforeTransaction)
                .add(new BigDecimal(amountOfTransaction))
                .toPlainString();

        doTransactionByCreatedUserToFirstAccount(anotherUserName, defaultUser, amountOfTransaction);

        firstAccount.accountMoneyShouldHave(text(expectedMoneyInTheAccountToAfterTransaction));
        accountsPage.checkPushAboutTransactionFromAnotherUserAndCloseThem(anotherUserName);
    }

    @Test
    void userGetMessageAboutTransactionExecutedByAdminUnderHim() {
        String defaultUser = "user";
        String anotherUserName = "anotherUserCheckAdminPush";
        String amountOfTransaction = "100";

        createUserByAdmin(anotherUserName); //То что созданный юзер имеет дефолтный счет проверялось на апи тестах
        login(defaultUser, "pass");
        $(byText("Accounts")).click();

        AccountsPage accountsPage = new AccountsPage();
        Account firstAccount = accountsPage.getAccountWithIndex(0);
        String moneyAccountToBeforeTransaction = firstAccount.getMoneyAmount();
        String expectedMoneyInTheAccountToAfterTransaction = new BigDecimal(moneyAccountToBeforeTransaction)
                .subtract(new BigDecimal(amountOfTransaction))
                .toPlainString();

        doTransactionByAdminToFirstAccount(defaultUser, anotherUserName, amountOfTransaction);

        firstAccount.accountMoneyShouldHave(text(expectedMoneyInTheAccountToAfterTransaction));
        accountsPage.checkPushAboutYourTransactionAndCloseThem();
    }

    @Test
    void adminCanDoTransactionFromAnotherUser() {
        String adminName = "admin";
        String defaultUserName = "user";
        String amountOfTransaction = "100";

        login(adminName, "pass");
        $(byText("Accounts")).click();

        AccountsPage accountsPage = new AccountsPage();
        List<Account> accountsOfDefaultUser = accountsPage.getAccountsByUserName(defaultUserName);
        Account accountFrom = accountsOfDefaultUser.get(0);
        Account accountTo = accountsOfDefaultUser.get(1);

        String moneyAccountFromBeforeTransaction = accountFrom.getMoneyAmount();
        String moneyAccountToBeforeTransaction = accountTo.getMoneyAmount();

        String expectedMoneyInTheAccountFromAfterTransaction = new BigDecimal(moneyAccountFromBeforeTransaction)
                .subtract(new BigDecimal(amountOfTransaction))
                .toPlainString();

        String expectedMoneyInTheAccountToAfterTransaction = new BigDecimal(moneyAccountToBeforeTransaction)
                .add(new BigDecimal(amountOfTransaction))
                .toPlainString();

        accountFrom
                .beginTransaction()
                .chooseAccountToFromSuggestionsByAdmin(adminName, accountTo)
                .setAmountOfTransaction(amountOfTransaction)
                .execute();

        accountTo.accountMoneyShouldHave(text(expectedMoneyInTheAccountToAfterTransaction));
        accountFrom.accountMoneyShouldHave(text(expectedMoneyInTheAccountFromAfterTransaction));
        accountsPage.checkPushAboutYourTransactionAndCloseThem(); //Админ получает пуш как будто бы он сам сделал эту транзакцию
    }

    @Test
    void adminCanDoTransactionToUser() {
        String adminName = "admin";
        String defaultUserName = "user";
        String amountOfTransaction = "100000";

        login(adminName, "pass");
        $(byText("Accounts")).click();

        AccountsPage accountsPage = new AccountsPage();
        List<Account> accountsOfDefaultUser = accountsPage.getAccountsByUserName(defaultUserName);
        List<Account> accountsOfAdmin = accountsPage.getAccountsByUserName(adminName);
        Account accountFrom = accountsOfAdmin.get(0);
        Account accountTo = accountsOfDefaultUser.get(0);

        String moneyAccountFromBeforeTransaction = accountFrom.getMoneyAmount();
        String moneyAccountToBeforeTransaction = accountTo.getMoneyAmount();

        String expectedMoneyInTheAccountToAfterTransaction = new BigDecimal(moneyAccountToBeforeTransaction)
                .add(new BigDecimal(amountOfTransaction))
                .toPlainString();

        accountFrom
                .beginTransaction()
                .chooseAccountToFromSuggestionsByAdmin(adminName, accountTo)
                .setAmountOfTransaction(amountOfTransaction)
                .execute();

        accountTo.accountMoneyShouldHave(text(expectedMoneyInTheAccountToAfterTransaction));
        accountFrom.accountMoneyShouldHave(text(moneyAccountFromBeforeTransaction));
        accountsPage.checkPushAboutYourTransactionAndCloseThem();
    }

    @Test
    void adminCanNotDeleteAccountsAnotherUser() {
        String adminName = "admin";
        String defaultUser = "user";

        login(adminName, "pass");
        $(byText("Accounts")).click();

        AccountsPage accountsPage = new AccountsPage();
        accountsPage
                .getAccountsByUserName(defaultUser)
                .forEach(account -> account.deleteButtonShouldBe(not(enabled)));
    }

    @Test
    void adminCanDeleteHisOwnAccount() {
        String adminName = "admin";

        login(adminName, "pass");
        $(byText("Accounts")).click();

        AccountsPage accountsPage = new AccountsPage();
        accountsPage.accountsOfUserShouldHave(adminName, size(2));

        accountsPage
                .getAccountsByUserName(adminName)
                .get(0)
                .beginDeleteAccount()
                .checkWarningMessage()
                .execute();

        accountsPage.accountsOfUserShouldHave(adminName, size(1));
    }
}
