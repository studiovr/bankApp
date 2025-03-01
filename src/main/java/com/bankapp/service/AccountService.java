package com.bankapp.service;

import com.bankapp.enums.AccountStatus;
import com.bankapp.enums.Currency;
import com.bankapp.enums.TransactionType;
import com.bankapp.exception.AccountNotFoundException;
import com.bankapp.exception.InsufficientFundsException;
import com.bankapp.repository.AccountRepository;
import com.bankapp.ui.transaction.TransferForm;
import com.bankapp.utils.TransactionManager;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.bankapp.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionManager transactionManager;
    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(AccountRepository accountRepository, TransactionManager transactionManager,
                          TransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.transactionManager = transactionManager;
        this.transactionService = transactionService;
    }

    public void createAccount(Account account) throws SQLException {
        if (accountRepository.isAccountNumberExists(account.getAccountNumber(),null)) {
            throw new SQLException("Номер счета уже существует");
        }

        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.OPEN);
        accountRepository.save(account);
    }

    public Account findAccountById(Long id) throws SQLException {
        return accountRepository.findById(id);
    }

    public List<Account> findAllAccounts() throws SQLException {
        return accountRepository.findAll();
    }

    public List<Account> findAccounts(int offset, int limit, List<QuerySortOrder> sortOrders) throws SQLException {
        return accountRepository.findAccounts(offset, limit, sortOrders);
    }

    public int countAccounts() throws SQLException {
        return accountRepository.countAccounts();
    }

    public List<Account> findAccountsByClientId(Long clientId) throws SQLException {
        return accountRepository.findByClientId(clientId);
    }

    public void updateAccount(Account account) throws SQLException {
        if (accountRepository.isAccountNumberExists(account.getAccountNumber(), account.getId())) {
            throw new SQLException("Номер счета уже существует");
        }

        accountRepository.update(account);
    }

    public void closeAccount(Long id) throws SQLException {
        accountRepository.closeAccount(id);
    }

    public boolean hasOpenAccounts() throws SQLException {
        return accountRepository.hasOpenAccounts();
    }

    public List<Account> findAccountsByCurrencyAndNotSenderId(String currency, Long senderId) throws SQLException {
        return accountRepository.findAccountsByCurrencyAndNotSenderId(currency, senderId);
    }

    public List<Account> findAccountsByCurrencyAndNotSenderIdAndClientId(String currency, Long senderId, Long clientId) throws SQLException {
        return accountRepository.findAccountsByCurrencyAndNotSenderIdAndClientId(currency, senderId, clientId);
    }


    public void transferFunds(Long fromAccountId, Long toAccountId, BigDecimal amount, Currency currency) throws SQLException {
        try {
            transactionManager.beginTransaction();

            Account fromAccount = accountRepository.findById(fromAccountId);
            Account toAccount = accountRepository.findById(toAccountId);

            if (fromAccount == null || toAccount == null) {
                throw new AccountNotFoundException("Один из счетов не найден");
            }

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Недостаточно средств на счете отправителя");
            }

            if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
                throw new IllegalArgumentException("Нельзя переводить между счетами с разной валютой");
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));

            accountRepository.update(fromAccount);
            accountRepository.update(toAccount);

            transactionService.createTransaction(fromAccountId, toAccountId, TransactionType.TRANSFER, amount,
                    currency);

            logger.info("Перевод с счета {} на счет {} на сумму {} выполнен успешно", fromAccountId, toAccountId, amount);

            transactionManager.commitTransaction();
        } catch (Exception e) {
            logger.error("Ошибка при выполнении перевода", e);
            transactionManager.rollbackTransaction();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else if (e instanceof AccountNotFoundException) {
                throw (AccountNotFoundException) e;
            } else if (e instanceof InsufficientFundsException) {
                throw (InsufficientFundsException) e;
            } else {
                throw new SQLException(e);
            }
        }
    }

    public void depositFunds(Long accountId, BigDecimal amount) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма зачисления должна быть положительной");
        }

        try {
            transactionManager.beginTransaction();
            Account account = accountRepository.findById(accountId);
            if (account == null) {
                throw new AccountNotFoundException("Счет не найден");
            }

            account.setBalance(account.getBalance().add(amount));
            accountRepository.update(account);
            transactionService.createTransaction(null, accountId, TransactionType.CREDIT,
                    amount, account.getCurrency());
            transactionManager.commitTransaction();
        } catch (Exception e) {
            logger.error("Ошибка при выполнении перевода", e);
            transactionManager.rollbackTransaction();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else if (e instanceof AccountNotFoundException) {
                throw (AccountNotFoundException) e;
            } else if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            } else {
                throw new SQLException(e);
            }
        }
    }
}
