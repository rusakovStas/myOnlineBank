package com.stasdev.backend.errors;

public class UserIsAlreadyExist extends RuntimeException {
    public UserIsAlreadyExist(String message) {
        super(message);
    }
}
