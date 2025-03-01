package com.bankapp.enums;

public enum Currency {
    RUB("Российский рубль"),
    USD("Доллар США"),
    EUR("Евро");

    private final String description;

    Currency(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}