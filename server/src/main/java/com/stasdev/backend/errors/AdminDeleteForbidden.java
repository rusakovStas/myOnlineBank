package com.stasdev.backend.errors;

public class AdminDeleteForbidden extends RuntimeException {
    public AdminDeleteForbidden(String message) {
        super(message);
    }
}
