package com.stasdev.backend.errors;

public class NotEnoughAmountOnAccount extends RuntimeException {
    public NotEnoughAmountOnAccount(String message) {
        super(message);
    }
}
