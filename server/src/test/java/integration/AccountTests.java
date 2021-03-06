package integration;

import com.codeborne.selenide.*;
import common.TestProperties;
import integration.po.Account;
import integration.po.AccountsPage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.hamcrest.MatcherAssert.assertThat;


public class AccountTests extends CommonUITest {

    private DecimalFormat decimalFormatter = new DecimalFormat("#,###.##", new DecimalFormatSymbols(new Locale("ru", "RU")));;

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
        int sizeBeforeDeleting = getAccountsOfDefaultUser().size();

        accountsPage
                .getAccountWithIndex(0)
                .beginDeleteAccount()
                .checkWarningMessage()
                .execute();

        accountsPage.accountsShouldHave(size(sizeBeforeDeleting - 1));
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
        String expectedMoneyInTheAccountAfterTransaction = decimalFormatter.format(new BigDecimal(moneyInTheAmountBeforeTransaction)
                .subtract(new BigDecimal(amountOfTransaction)));

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

        String expectedMoneyInTheAccountFromAfterTransaction = decimalFormatter.format(new BigDecimal(moneyAccountFromBeforeTransaction)
                .subtract(new BigDecimal(amountOfTransaction)));

        String expectedMoneyInTheAccountToAfterTransaction = decimalFormatter.format(new BigDecimal(moneyAccountToBeforeTransaction)
                .add(new BigDecimal(amountOfTransaction)));

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

        String expectedMoneyInTheAccountToAfterTransaction = decimalFormatter.format(new BigDecimal(moneyAccountToBeforeTransaction)
                .add(new BigDecimal(amountOfTransaction)));

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
        String expectedMoneyInTheAccountToAfterTransaction = decimalFormatter.format(new BigDecimal(moneyAccountToBeforeTransaction)
                .subtract(new BigDecimal(amountOfTransaction)));

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

        String expectedMoneyInTheAccountFromAfterTransaction = decimalFormatter.format(new BigDecimal(moneyAccountFromBeforeTransaction)
                .subtract(new BigDecimal(amountOfTransaction)));

        String expectedMoneyInTheAccountToAfterTransaction = decimalFormatter.format(new BigDecimal(moneyAccountToBeforeTransaction)
                .add(new BigDecimal(amountOfTransaction)));

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
        String amountOfTransaction = "9000000";

        login(adminName, "pass");
        $(byText("Accounts")).click();

        AccountsPage accountsPage = new AccountsPage();
        List<Account> accountsOfDefaultUser = accountsPage.getAccountsByUserName(defaultUserName);
        List<Account> accountsOfAdmin = accountsPage.getAccountsByUserName(adminName);
        Account accountFrom = accountsOfAdmin.get(0);
        Account accountTo = accountsOfDefaultUser.get(0);

        String moneyAccountFromBeforeTransaction = accountFrom.getMoneyAmount();
        String moneyAccountToBeforeTransaction = accountTo.getMoneyAmount();

        String expectedMoneyInTheAccountToAfterTransaction = decimalFormatter.format(new BigDecimal(moneyAccountToBeforeTransaction)
                .add(new BigDecimal(amountOfTransaction)));

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
        int sizeBeforeDeleting = getAllAccountsByAdmin()
                .stream()
                .filter(a -> a.getUser().getUsername().equals(adminName))
                .collect(Collectors.toList())
                .size();

        accountsPage
                .getAccountsByUserName(adminName)
                .get(0)
                .beginDeleteAccount()
                .checkWarningMessage()
                .execute();

        accountsPage.accountsOfUserShouldHave(adminName, size(sizeBeforeDeleting - 1));
    }

    @Test
    void userCanCreateNewAccountAndDoTransactionToIt() {
        String newUser = "newUser";
        String amountOfTransaction = "100";
        createUserByAdmin(newUser); //То что созданный юзер имеет дефолтный счет проверялось на апи тестах
        login(newUser, DEFAULT_PASSWORD);
        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();
        accountsPage.createNewAccount();

        accountsPage.accountsShouldHave(size(2));
        Account createdAccount = accountsPage.getAccountWithIndex(1);
        Account firstAccount = accountsPage.getAccountWithIndex(0);

        firstAccount.beginTransaction()
                .chooseAccountToFromSuggestions(createdAccount)
                .setAmountOfTransaction(amountOfTransaction)
                .execute();

        createdAccount.accountMoneyShouldHave(text(amountOfTransaction));
        accountsPage.checkPushAboutTransactionToOwnAccountAndCloseThem(newUser);
    }

    @Test
    void adminCanCreateNewAccount() {
        String adminName = "admin";

        login(adminName, "pass");
        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();

        Account newAccount = accountsPage.createNewAccount();

        newAccount.accountNameShouldHave(not(exist));
    }

    @Test
    void userCanNotSeeAccountInSuggestionsWhichDeleted() {
        String newUserForCheckDeleteSuggestions = "newUserForCheckDeleteSuggestions";
        createUserByAdmin(newUserForCheckDeleteSuggestions);
        login(newUserForCheckDeleteSuggestions, DEFAULT_PASSWORD);
        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();
        accountsPage.getCreateNewAccountButton().shouldBe(enabled).click();
        accountsPage.accountsShouldHave(size(2));

        Account accountForDelete = accountsPage.getAccountWithIndex(1);
        String number = accountForDelete.getNumber();
        accountForDelete
                .beginDeleteAccount()
                .checkWarningMessage()
                .execute();

        accountsPage.getAccountWithIndex(0)
                .beginTransaction()
                .checkThatSuggestionsNotHaveAccountTo("My ", number);
    }

    @Test
    void userCanRenameHisOwnAccount() {
        String defaultUser = "user";
        login(defaultUser, "pass");
        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();
        Account firstAccount = accountsPage.getAccountWithIndex(0);
        String newName = "New Name";

        firstAccount
                .editNameOfAccount(newName)
                .execute();

        firstAccount.accountNameShouldHave(value(newName));
    }

    @Test
    void userCanNameNotNamedAccount() {
        String defaultUser = "user";
        login(defaultUser, "pass");
        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();
        String newName = "New name for new account";
        Account newAccount = accountsPage.createNewAccount();

        newAccount
                .editNameOfAccount(newName)
                .execute();

        newAccount.accountNameShouldHave(value(newName));
    }

    @Test
    void adminCanRenameHisOwnAccount() {
        String adminName = "admin";

        login(adminName, "pass");
        $(byText("Accounts")).click();

        AccountsPage accountsPage = new AccountsPage();

        String newAdminAccountName = "New Admin account name";
        Account firstAdminAccount = accountsPage
                .getAccountsByUserName(adminName)
                .get(0);
        firstAdminAccount
                .editNameOfAccount(newAdminAccountName)
                .execute();
        firstAdminAccount.accountNameShouldHave(value(newAdminAccountName));
    }

    @Test
    void adminCanNotRenameNotHisOwnAccount() {
        String adminName = "admin";
        String defaultUser = "user";

        login(adminName, "pass");
        $(byText("Accounts")).click();

        AccountsPage accountsPage = new AccountsPage();
        accountsPage
                .getAccountsByUserName(defaultUser)
                .forEach(account -> account.editButtonShouldBe(not(enabled)));
    }

    @Test
    void userCanNotSendMoneyMoreThanHeHaveOnAccount() {
        String defaultUser = "user";
        login(defaultUser, "pass");
        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();
        Account firstAccount = accountsPage.getAccountWithIndex(0);
        Account secondAccount = accountsPage.getAccountWithIndex(1);
        String amountOfAccount = firstAccount.getMoneyAmount();

        firstAccount
                .beginTransaction()
                .chooseAccountToFromSuggestions(secondAccount)
                .setAmountOfTransaction(amountOfAccount + "0")
                .execute();

        firstAccount.notEnoughMoneyErrorShoultBe(visible);
    }

    @Test
    void userNotSeeHisOwnAccountInSuggestions() {
        String defaultUser = "user";
        login(defaultUser, "pass");
        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();
        Account firstAccount = accountsPage.getAccountWithIndex(0);

        firstAccount
                .beginTransaction()
                .checkThatSuggestionsNotHaveAccountTo("My ", firstAccount.getNumber());
    }

    @Test
    void adminCanSendMoneyToHimselfByAnotherUser() {
        String adminName = "admin";
        String defaultUser = "user";
        String amount = "100";

        login(adminName, "pass");
        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();
        Account firstAccountOfDefaultUser = accountsPage.getAccountsByUserName(defaultUser).get(0);
        Account adminAccount = accountsPage.getAccountsByUserName(adminName).get(0);
        String moneyAccountFromBeforeTransaction = firstAccountOfDefaultUser.getMoneyAmount();
        String expectedMoneyInTheAccountFromAfterTransaction = decimalFormatter.format(new BigDecimal(moneyAccountFromBeforeTransaction)
                .subtract(new BigDecimal(amount)));

        firstAccountOfDefaultUser
                .beginTransaction()
                .chooseAccountToFromSuggestionsByAdmin(adminName, adminAccount)
                .setAmountOfTransaction(amount)
                .execute();

        Account firstAccountOfDefaultUserAfter = accountsPage.getAccountsByUserName(defaultUser).get(0);
        firstAccountOfDefaultUserAfter.accountMoneyShouldHave(text(expectedMoneyInTheAccountFromAfterTransaction));
        accountsPage.checkPushAboutTransactionToOwnAccountAndCloseThem(defaultUser);
    }

    @Test
    void userCanNotTypeInvalidAmount() {
        String defaultUser = "user";
        login(defaultUser, "pass");
        $(byText("Accounts")).click();
        AccountsPage accountsPage = new AccountsPage();
        Account firstAccount = accountsPage.getAccountWithIndex(0);
        Account secondAccount = accountsPage.getAccountWithIndex(1);

        firstAccount
                .beginTransaction()
                .chooseAccountToFromSuggestions(secondAccount)
                .setAmountOfTransactionToCheck("-10000.111")
                .checkAmountOfTransaction("10 000.11");
    }
}