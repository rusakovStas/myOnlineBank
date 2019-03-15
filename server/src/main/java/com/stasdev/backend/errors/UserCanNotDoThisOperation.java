package com.stasdev.backend.errors;

public class UserCanNotDoThisOperation extends RuntimeException{
    public UserCanNotDoThisOperation(String message) {
        super(message);
    }
}
