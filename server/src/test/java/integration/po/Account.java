package integration.po;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.Optional;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class Account {

    private SelenideElement accountItem;
    private SelenideElement owner;
    private SelenideElement number;
    private SelenideElement editButton;
    private SelenideElement editInput;
    private SelenideElement moneyAmount;
    private SelenideElement deleteButton;
    private SelenideElement acceptButton;
    private SelenideElement declineButton;
    private SelenideElement warningMessage;
    private SelenideElement transactionInput;
    private SelenideElement transactionButton;
    private SelenideElement transactionAmountInput;
    private ElementsCollection transactionSuggestions;

    public Account(SelenideElement accountItem) {
        this.accountItem = Optional.of(accountItem)
                .orElseThrow(() -> new RuntimeException("Account item can't be null"));
        this.owner = accountItem.$("#card-owner");
        this.number = accountItem.$("#account-number");
        this.editInput = accountItem.$("#accountName");
        this.editButton = accountItem.$("#updateButton");
        this.moneyAmount = accountItem.$("#money-in-the-account");
        this.deleteButton = accountItem.$(byText("Block"));
        this.acceptButton = accountItem.$(byText("Accept"));
        this.declineButton = accountItem.$(byText("Decline"));
        this.warningMessage = accountItem.$(byText("Are you sure you want to block the account?"));
        this.transactionInput = accountItem.$(".transaction-input-test input:enabled");
        this.transactionButton = accountItem.$(byText("Transaction"));
        this.transactionAmountInput = accountItem.$("#amount");
        this.transactionSuggestions = accountItem.$$(".transaction-input-test li");
    }

    public void accountMoneyShouldHave(Condition condition){
        this.moneyAmount.shouldHave(condition);
    }

    public SelenideElement getAccountItem() {
        return accountItem;
    }

    public String getMoneyAmount() {
        return moneyAmount.getText();
    }

    public String getAccountName(){
        return editInput.shouldBe(disabled).getValue();
    }

    public void accountNameShouldHave(Condition condition){
        this.editInput.shouldBe(disabled).shouldHave(condition);
    }

    public String getNumber() {
        return number.getText();
    }

    public String getOwnerName() {
        return owner.getText().replace("Owner: ","");
    }

    public TerminalOperation editNameOfAccount(String newName){
        number.scrollTo();
        number.click();
        editButton.scrollTo().shouldBe(enabled).click();
        editInput.shouldBe(enabled).setValue(newName);
        return new TerminalOperation();
    }

    public Transaction beginTransaction(){
        number.scrollTo();
        number.click();
        transactionButton.click();
        return new Transaction();
    }

    public void deleteButtonShouldBe(Condition condition){
        number.scrollTo();
        number.click();
        this.deleteButton.shouldBe(condition);
    }

    public void editButtonShouldBe(Condition condition){
        number.scrollTo();
        number.click();
        this.editButton.scrollTo().shouldBe(condition);
    }

    public DeleteAccount beginDeleteAccount() {
        number.scrollTo();
        number.click();
        deleteButton.click();
        return new DeleteAccount();
    }

     public class DeleteAccount {
        DeleteAccount(){}

        public TerminalOperation checkWarningMessage(){
            warningMessage.shouldBe(visible);
            return new TerminalOperation();
        }
    }

     public class Transaction {

        Transaction(){}

        public Transaction chooseAccountToFromSuggestions(Account accountTo){
             if (getOwnerName().equals(accountTo.getOwnerName())){
                 transactionInput.setValue("My own account");
             }else {
                 transactionInput.setValue(accountTo.getOwnerName());
             }
             transactionInput.scrollTo();//что бы suggestions уместились
             transactionSuggestions
                     .shouldHave(sizeGreaterThan(0))
                     .exclude(not(text(accountTo.getNumber().split(" ")[3])))
                     .shouldHave(size(1))
                     .first()
                     .click();
             return this;
         }

         public Transaction chooseAccountToFromSuggestionsByAdmin(String adminName, Account accountTo){
             if (adminName.equals(accountTo.getOwnerName())){
                 transactionInput.setValue("My own account");
             }else {
                 transactionInput.setValue(accountTo.getOwnerName());
             }
             transactionInput.scrollTo();//что бы suggestions уместились
             transactionSuggestions
                     .shouldHave(sizeGreaterThan(0))
                     .exclude(not(text(accountTo.getNumber().split(" ")[3])))
                     .shouldHave(size(1))
                     .first()
                     .click();
             return this;
         }

         public Transaction chooseAccountToFromSuggestions(String userName, String numberOfAccount){
             transactionInput.setValue(userName);
             transactionSuggestions
                     .shouldHave(sizeGreaterThan(0))
                     .exclude(not(text(userName)))
                     .exclude(not(text(numberOfAccount.split(" ")[3])))
                     .shouldHave(size(1))
                     .first()
                     .click();
             return this;
         }

         public void checkThatSuggestionsNotHaveAccountTo(String userName, String numberOfAccount){
             transactionInput.setValue(userName);
             transactionSuggestions
                     .shouldHave(sizeGreaterThan(0))
                     .exclude(not(text(userName)))
                     .exclude(not(text(numberOfAccount.split(" ")[3])))
                     .shouldHave(size(0));
         }

        public TerminalOperation setAmountOfTransaction(String amount){
            transactionAmountInput.setValue(amount);
            return new TerminalOperation();
        }

    }

      public class TerminalOperation {
        TerminalOperation() {}

        public void execute(){
            acceptButton.scrollTo().shouldBe(enabled).click();
        }

        public void decline(){
            declineButton.scrollTo().shouldBe(enabled).click();
        }

    }

}
