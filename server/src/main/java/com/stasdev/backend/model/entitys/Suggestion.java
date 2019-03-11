package com.stasdev.backend.model.entitys;

public class Suggestion {

    private String maskAccountNumber;
    private String userName;
    private Long accountId;

    public Suggestion(String maskAccountNumber, String userName, Long accountId) {
        this.maskAccountNumber = maskAccountNumber;
        this.userName = userName;
        this.accountId = accountId;
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
