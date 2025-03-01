package com.bankapp.service;

import com.bankapp.enums.Currency;
import com.bankapp.enums.TransactionType;
import com.bankapp.exception.TransactionException;
import com.bankapp.model.Transaction;
import com.bankapp.repository.TransactionRepositoryImpl;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class TransactionService {

    private final TransactionRepositoryImpl transactionRepositoryImpl;
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(TransactionRepositoryImpl transactionRepositoryImpl) {
        this.transactionRepositoryImpl = transactionRepositoryImpl;
    }

    public void createTransaction(Long accountId, Long targetAccountId, TransactionType type, BigDecimal amount, Currency currency) {
        try {
            Transaction transaction = new Transaction();
            transaction.setFromAccount(accountId);
            transaction.setToAccount(targetAccountId);
            transaction.setAmount(amount);
            transaction.setCurrency(currency);
            transaction.setStatus(type);
            transactionRepositoryImpl.save(transaction);
        } catch (SQLException e) {
            logger.error("Ошибка при создании транзакции", e);
            throw new TransactionException("Ошибка при создании транзакции", e);
        }
    }

    public List<Transaction> findAllTransactions() {
        try {
            return transactionRepositoryImpl.findAll();
        } catch (SQLException e) {
            logger.error("Ошибка при получении списка транзакций", e);
            throw new TransactionException("Ошибка при получении списка транзакций", e);
        }
    }

    public List<Transaction> findTransactions(int offset, int limit, List<QuerySortOrder> sortOrders) {
        try {
            return transactionRepositoryImpl.findTransactions(offset, limit, sortOrders);
        } catch (SQLException e) {
            logger.error("Ошибка при получении списка транзакций по фильтру", e);
            throw new TransactionException("Ошибка при получении списка транзакций по фильтру", e);
        }
    }

    public int countTransactions() {
        try {
            return transactionRepositoryImpl.count();
        } catch (SQLException e) {
            logger.error("Ошибка при подсчёте транзакций", e);
            throw new TransactionException("Ошибка при подсчёте транзакций", e);
        }
    }
}