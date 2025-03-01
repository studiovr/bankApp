package com.bankapp.service;

import com.bankapp.enums.Currency;
import com.bankapp.enums.TransactionType;
import com.bankapp.model.Account;
import com.bankapp.model.Transaction;
import com.bankapp.repository.TransactionRepository;
import com.vaadin.flow.data.provider.QuerySortOrder;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void createTransaction(Long accountId, Long targetAccountId, TransactionType type, BigDecimal amount, Currency currency) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(accountId);
        transaction.setToAccountId(targetAccountId);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setStatus(type);
        transactionRepository.save(transaction);
    }

    public List<Transaction> findAllTransactions() throws SQLException {
        return transactionRepository.findAll();
    }

    public List<Transaction> findTransactions(int offset, int limit, List<QuerySortOrder> sortOrders) throws SQLException {
        return transactionRepository.findTransactions(offset, limit, sortOrders);
    }

    public int countTransactions() throws SQLException {
        return transactionRepository.countTransactions();
    }
}