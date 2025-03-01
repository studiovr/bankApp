package com.bankapp.enums;

public enum AccountStatus {
    OPEN("Открыт"),
    CLOSED("Закрыт");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
