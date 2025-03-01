package com.bankapp.exception;

public class PhoneNumberExistException extends RuntimeException {
    public PhoneNumberExistException(String message) {
        super(message);
    }
}
