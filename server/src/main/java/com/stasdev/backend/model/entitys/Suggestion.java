package com.stasdev.backend.model.entitys;

/*
* Специальный класс через которую мы передаем
* необходимую для совершения транзакции информацию клиенту
* */
public class Suggestion {

    private String maskAccountNumber;
    private String userName;
    private Long accountId;

    public Suggestion(String accountNumber, String userName, Long accountId) {
        this.maskAccountNumber = maskAccount(accountNumber);
        this.userName = userName;
        this.accountId = accountId;
    }

    public Suggestion() {
    }

    private String maskAccount(String account){
        return "*** " + account.split(" ")[3];
    }

    public String getMaskAccountNumber() {
        return maskAccountNumber;
    }

    public void setMaskAccountNumber(String maskAccountNumber) {
        this.maskAccountNumber = maskAccountNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
