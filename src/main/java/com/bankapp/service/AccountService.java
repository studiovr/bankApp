package com.bankapp.service;

import com.bankapp.enums.AccountStatus;
import com.bankapp.enums.Currency;
import com.bankapp.enums.TransactionType;
import com.bankapp.exception.AccountExistException;
import com.bankapp.exception.AccountNotFoundException;
import com.bankapp.exception.DataAccessException;
import com.bankapp.exception.InsufficientFundsException;
import com.bankapp.repository.AccountRepositoryImpl;
import com.bankapp.utils.TransactionManager;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.bankapp.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AccountService {
    private final AccountRepositoryImpl accountRepositoryImpl;
    private final TransactionManager transactionManager;
    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(AccountRepositoryImpl accountRepositoryImpl, TransactionManager transactionManager,
                          TransactionService transactionService) {
        this.accountRepositoryImpl = accountRepositoryImpl;
        this.transactionManager = transactionManager;
        this.transactionService = transactionService;
    }

    public void createAccount(Account account){
        try {
            if (accountRepositoryImpl.isAccountNumberExists(account.getAccountNumber(),null)) {
                throw new AccountExistException("Номер счета уже существует");
            }

            account.setBalance(BigDecimal.ZERO);
            account.setStatus(AccountStatus.OPEN);
            accountRepositoryImpl.save(account);
            logger.info("Счет успешно сохранен, номер счета={}", account.getAccountNumber());
        } catch (SQLException e) {
            logger.error("Ошибка при создании счета: {}", e.getMessage(), e);
            throw new DataAccessException("Ошибка при создании счета", e);
        }
    }

    public Account findAccountById(Long id) {
        try {
            return accountRepositoryImpl.findById(id)
                    .orElseThrow(() -> new AccountNotFoundException("Счет с id=" + id + " не найден"));
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при поиске счета", e);
        }
    }

    public List<Account> findAllAccounts(){
        try {
            return accountRepositoryImpl.findAll();
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при получении списка счетов", e);
        }
    }

    public List<Account> findAccounts(int offset, int limit, List<QuerySortOrder> sortOrders) {
        try {
            return accountRepositoryImpl.findAccounts(offset, limit, sortOrders);
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке счетов", e);
            throw new DataAccessException("Ошибка при получении списка счетов по фильтру", e);
        }
    }

    public int countAccounts() {
        try {
            return accountRepositoryImpl.count();
        } catch (SQLException e) {
            logger.error("Ошибка при подсчёте счетов", e);
            throw new DataAccessException("Ошибка при получении количества счетов", e);
        }
    }

    public List<Account> findAccountsByClientId(Long clientId) {
        try {
            return accountRepositoryImpl.findByClientId(clientId);
        } catch (SQLException e) {
            logger.error("Ошибка при получении счета по клиенту", e);
            throw new DataAccessException("Ошибка при получении счета по клиенту", e);
        }
    }

    public void updateAccount(Account account)  {
        try {
            if (accountRepositoryImpl.isAccountNumberExists(account.getAccountNumber(), account.getId())) {
                throw new AccountExistException("Номер счета уже существует");
            }

            accountRepositoryImpl.update(account);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при обновлении счета", e);
        }
    }

    public void closeAccount(Long id) {
        try {
            accountRepositoryImpl.closeAccount(id);
        } catch (SQLException e) {
            logger.error("Ошибка при закрытии счета", e);
            throw new DataAccessException("Ошибка при закрытии счета", e);
        }
    }

    public boolean hasOpenAccounts() {
        try {
            return accountRepositoryImpl.hasOpenAccounts();
        } catch (SQLException e) {
            logger.error("Ошибка при проверке открытых счетов", e);
            throw new DataAccessException("Ошибка при проверке открытых счетов", e);
        }
    }

    public List<Account> findAccountsByCurrencyAndNotSenderId(String currency, Long senderId) {
        try {
            return accountRepositoryImpl.findAccountsByCurrencyAndNotSenderId(currency, senderId);
        } catch (SQLException e) {
            logger.error("Ошибка при поиске счетов по валюте и отправителю", e);
            throw new DataAccessException("Ошибка при поиске счетов по валюте и отправителю", e);
        }
    }

    public List<Account> findAccountsByCurrencyAndNotSenderIdAndClientId(String currency, Long senderId, Long clientId) {
        try {
            return accountRepositoryImpl.findAccountsByCurrencyAndNotSenderIdAndClientId(currency, senderId, clientId);
        } catch (SQLException e) {
            logger.error("Ошибка при поиске счетов по валюте, отправителю и клиенту", e);
            throw new DataAccessException("Ошибка при поиске счетов по валюте, отправителю и клиенту", e);
        }
    }


    public void transferFunds(Long fromAccountId, Long toAccountId, BigDecimal amount, Currency currency) {
        try {
            transactionManager.beginTransaction();

            Optional<Account> fromAccountOpt = accountRepositoryImpl.findById(fromAccountId);
            Optional<Account> toAccountOpt = accountRepositoryImpl.findById(toAccountId);

            if (fromAccountOpt.isEmpty() || toAccountOpt.isEmpty()) {
                throw new AccountNotFoundException("Один из счетов не найден");
            }

            Account fromAccount = fromAccountOpt.get();
            Account toAccount = toAccountOpt.get();

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Недостаточно средств на счете отправителя");
            }

            if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
                throw new IllegalArgumentException("Нельзя переводить между счетами с разной валютой");
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));

            accountRepositoryImpl.update(fromAccount);
            accountRepositoryImpl.update(toAccount);

            transactionService.createTransaction(fromAccountId, toAccountId, TransactionType.TRANSFER, amount,
                    currency);

            logger.info("Перевод с счета {} на счет {} на сумму {} выполнен успешно", fromAccountId, toAccountId, amount);

            transactionManager.commitTransaction();
        } catch (Exception e) {
            logger.error("Ошибка при выполнении перевода", e);
            try {
                transactionManager.rollbackTransaction();
            } catch (SQLException ex) {
                logger.error("Ошибка при откате транзакции", ex);
                throw new DataAccessException("Ошибка при откате транзакции", ex);
            }
            if (e instanceof AccountNotFoundException) {
                throw (AccountNotFoundException) e;
            } else if (e instanceof InsufficientFundsException) {
                throw (InsufficientFundsException) e;
            } else {
                throw new DataAccessException("Ошибка при выполнении перевода", e);
            }
        }
    }

    public void depositFunds(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма зачисления должна быть положительной");
        }

        try {
            transactionManager.beginTransaction();

            Optional<Account> accountOpt = accountRepositoryImpl.findById(accountId);

            if (accountOpt.isEmpty()) {
                throw new AccountNotFoundException("Счет не найден");
            }

            Account account = accountOpt.get();

            account.setBalance(account.getBalance().add(amount));
            accountRepositoryImpl.update(account);
            transactionService.createTransaction(null, accountId, TransactionType.CREDIT,
                    amount, account.getCurrency());
            transactionManager.commitTransaction();
        } catch (Exception e) {
            logger.error("Ошибка при выполнении перевода", e);
            try {
                transactionManager.rollbackTransaction();
            } catch (SQLException ex) {
                logger.error("Ошибка при откате транзакции", ex);
                throw new DataAccessException("Ошибка при откате транзакции", ex);
            }
            if (e instanceof AccountNotFoundException) {
                throw (AccountNotFoundException) e;
            } else if (e instanceof InsufficientFundsException) {
                throw (InsufficientFundsException) e;
            } else {
                throw new DataAccessException("Ошибка при выполнении перевода", e);
            }
        }
    }
}
