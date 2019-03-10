package com.stasdev.backend.errors;

public class ThereIsNoAccountsWithId extends RuntimeException{
    public ThereIsNoAccountsWithId(String message) {
        super(message);
    }
}
