package com.bankapp.model;

import com.bankapp.enums.AccountStatus;
import com.bankapp.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Account {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountStatus status;
    private String bik;
    private Currency currency;
    private Long clientId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Account() {
    }

    public Account(Long id, String accountNumber, BigDecimal  balance, AccountStatus status, String bik, Currency currency, Long clientId) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
        this.bik = bik;
        this.currency = currency;
        this.clientId = clientId;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal  getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal  balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getBik() {
        return bik;
    }

    public void setBik(String bik) {
        this.bik = bik;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id &&
                balance.compareTo(account.balance) == 0 &&
                clientId == account.clientId &&
                Objects.equals(accountNumber, account.accountNumber) &&
                Objects.equals(status, account.status) &&
                Objects.equals(bik, account.bik) &&
                Objects.equals(currency, account.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber, balance, status, bik, currency, clientId);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", status='" + status + '\'' +
                ", bik='" + bik + '\'' +
                ", currency='" + currency + '\'' +
                ", clientId=" + clientId +
                '}';
    }
}