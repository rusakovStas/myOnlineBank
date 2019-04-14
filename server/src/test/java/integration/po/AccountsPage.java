package integration.po;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$$;

public class AccountsPage {

    public AccountsPage(){
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
                .exclude(Condition.not(Condition.text(userName)))
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
                .exclude(Condition.not(Condition.text(userName)))
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
