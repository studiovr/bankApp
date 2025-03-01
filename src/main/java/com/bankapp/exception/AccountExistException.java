package com.bankapp.exception;

public class AccountExistException extends RuntimeException {
    public AccountExistException(String message) {
        super(message);
    }
}