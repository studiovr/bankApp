package com.bankapp.enums;

public enum TransactionType {
    TRANSFER("Перевод"),
    CREDIT("Зачисление");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}