package com.bankapp.model;

import com.bankapp.enums.Currency;
import com.bankapp.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {

    private Long id;
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal amount;
    private Currency currency;
    private LocalDateTime transactionDate;
    private TransactionType type;

    public Transaction() {
    }

    public Transaction(Long fromAccount, Long toAccount, BigDecimal amount, Currency currency, LocalDateTime transactionDate, TransactionType type) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.transactionDate = transactionDate;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Long fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Long getToAccount() {
        return toAccount;
    }

    public void setToAccount(Long toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getType() {
        return type;
    }

    public void setStatus(TransactionType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(fromAccount, that.fromAccount) &&
                Objects.equals(toAccount, that.toAccount) &&
                Objects.equals(amount, that.amount) &&
                currency == that.currency &&
                Objects.equals(transactionDate, that.transactionDate) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromAccount, toAccount, amount, currency, transactionDate, type);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", fromAccountId=" + fromAccount +
                ", toAccountId=" + toAccount +
                ", amount=" + amount +
                ", currency=" + currency +
                ", transactionDate=" + transactionDate +
                ", type=" + type +
                '}';
    }
}