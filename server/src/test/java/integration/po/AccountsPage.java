package integration.po;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.conditions.Text;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.CollectionCondition.texts;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class AccountsPage {

    public AccountsPage(){
    }

    public SelenideElement getCreateNewAccountButton() {
        return $(byText("Add new account"));
    }

    public Account createNewAccount(){
        List<String> numbersOfAccountsBeforeCreation = getAccounts().stream()
                .map(Account::getNumber)
                .collect(Collectors.toList());

        $(byText("Add new account")).shouldBe(enabled).click();

        SelenideElement newAccountSelenide = $$(".account-item")
                .exclude(new Contains("contains any texts", numbersOfAccountsBeforeCreation))
                .shouldHave(size(1))
                .get(0);
        return new Account(newAccountSelenide);
    }

    class Contains extends Condition{
        List<Text> textsCondition = new ArrayList<>();
        Contains(String name, List<String> texts) {
            super(name);
            for (String text: texts){
                textsCondition.add(new Text(text));
            }

        }

        @Override
        public boolean apply(Driver driver, WebElement element) {
            for (Text text : textsCondition) {
                if (text.apply(driver, element)){
                    return true;
                }
            }
            return false;
        }
    }

    private List<Account> getAccounts() {
        return $$(".account-item")
                .shouldHave(sizeGreaterThan(0))
                .stream()
                .map(Account::new)
                .collect(Collectors.toList());
    }

    public List<Account> getAccountsByUserName(String userName){
        return $$(".account-item")
                .shouldHave(sizeGreaterThan(0))
                .exclude(not(text(userName)))
                .shouldHave(sizeGreaterThan(0))
                .stream()
                .map(Account::new)
                .collect(Collectors.toList());
    }


    public Account getAccountWithIndex(int index){
        return getAccounts().get(index);
    }

    public void accountsShouldHave(CollectionCondition condition){
        $$(".account-item").shouldHave(condition);
    }

    public void accountsOfUserShouldHave(String userName, CollectionCondition condition){
        $$(".account-item")
                .shouldHave(sizeGreaterThan(0))
                .exclude(not(text(userName)))
                .shouldHave(condition);
    }

    public void checkPushAboutYourTransactionAndCloseThem(){
        $$(byText("Message from server")).shouldHave(size(1));
        $$(byText("Your transaction was successful"))
                .shouldHave(size(1))
                .get(0)
                .click();
        $$(byText("Message from server")).shouldHave(size(0));
        $$(byText("Your transaction was successful")).shouldHave(size(0));
    }

    public void checkPushAboutTransactionToOwnAccountAndCloseThem(String user){
        $$(byText("Message from server")).shouldHave(size(2));
        $$(byText("Your transaction was successful"))
                .shouldHave(size(1))
                .get(0)
                .click();
        $$(byText(String.format("User '%s' sent money for you", user)))
                .shouldHave(size(1))
                .get(0)
                .click();
        $$(byText("Message from server")).shouldHave(size(0));
        $$(byText("Your transaction was successful")).shouldHave(size(0));
        $$(byText(String.format("User '%s' sent money for you", user))).shouldHave(size(0));
    }


    public void checkPushAboutTransactionFromAnotherUserAndCloseThem(String anotherUser){
        $$(byText("Message from server")).shouldHave(size(1));
        $$(byText(String.format("User '%s' sent money for you", anotherUser)))
                .shouldHave(size(1))
                .get(0)
                .click();
        $$(byText("Message from server")).shouldHave(size(0));
        $$(byText(String.format("User '%s' sent money for you", anotherUser))).shouldHave(size(0));
    }
}
