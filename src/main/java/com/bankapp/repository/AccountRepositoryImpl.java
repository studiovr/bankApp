package com.bankapp.repository;

import com.bankapp.enums.AccountStatus;
import com.bankapp.enums.Currency;
import com.bankapp.exception.AccountExistException;
import com.bankapp.utils.TransactionManager;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.bankapp.model.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepositoryImpl extends AbstractRepository<Account, Long> {

    public AccountRepositoryImpl(TransactionManager transactionManager) {
        super(transactionManager);
    }

    @Override
    public void save(Account account) throws SQLException {
        if (isAccountNumberExists(account.getAccountNumber(),null)) {
            throw new AccountExistException("Номер счета уже существует");
        }

        String sql = "INSERT INTO accounts (account_number, balance, status, bik, currency, client_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, account.getAccountNumber());
            statement.setBigDecimal(2, account.getBalance());
            statement.setString(3, account.getStatus().toString());
            statement.setString(4, account.getBik());
            statement.setString(5, account.getCurrency().toString());
            statement.setLong(6, account.getClientId());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(Account account) throws SQLException {
        if (isAccountNumberExists(account.getAccountNumber(), account.getId())) {
            throw new AccountExistException("Номер счета уже существует");
        }

        String sql = "UPDATE accounts SET account_number = ?, balance = ?, status = ?, bik = ?, currency = ?, client_id = ? WHERE id = ?";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, account.getAccountNumber());
            statement.setBigDecimal(2, account.getBalance());
            statement.setString(3, account.getStatus().toString());
            statement.setString(4, account.getBik());
            statement.setString(5, account.getCurrency().toString());
            statement.setLong(6, account.getClientId());
            statement.setLong(7, account.getId());
            statement.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<Account> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapAccount(resultSet));
                }
            }
        }
        return Optional.empty();
    }


    public List<Account> findAccounts(int offset, int limit, List<QuerySortOrder> sortOrders) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM accounts");

        if (!sortOrders.isEmpty()) {
            sql.append(" ORDER BY ");
            for (QuerySortOrder sortOrder : sortOrders) {
                sql.append(mapSortColumn(sortOrder.getSorted()))
                        .append(sortOrder.getDirection().equals(SortDirection.ASCENDING) ? " ASC" : " DESC")
                        .append(", ");
            }
            sql.setLength(sql.length() - 2);
        } else {
            sql.append(" ORDER BY account_number ASC");
        }

        sql.append(" LIMIT ? OFFSET ?");

        try (PreparedStatement stmt = transactionManager.getConnection().prepareStatement(sql.toString())) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(mapAccount(rs));
            }
            return accounts;
        }
    }

    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM accounts";
        try (PreparedStatement stmt = transactionManager.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @Override
    public List<Account> findAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE status = 'OPEN'";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                accounts.add(mapAccount(resultSet));
            }
        }
        return accounts;
    }

    public List<Account> findByClientId(Long clientId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE client_id = ? AND status = 'OPEN'";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, clientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    accounts.add(mapAccount(resultSet));
                }
            }
        }
        return accounts;
    }

    public boolean hasOpenAccounts() throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM accounts WHERE status = 'OPEN')";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getBoolean(1);
            }
        }
        return false;
    }

    public void closeAccount(Long accountId) throws SQLException {
        String sql = "UPDATE accounts SET status = 'CLOSED' WHERE id = ?";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.executeUpdate();
        }
    }

    public List<Account> findAccountsByCurrencyAndNotSenderId(String currency, Long senderId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE currency = ? AND id != ?";

        try (PreparedStatement stmt = transactionManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, currency);
            stmt.setLong(2, senderId);
            ResultSet rs = stmt.executeQuery();

            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(mapAccount(rs));
            }
            return accounts;
        }
    }

    public List<Account> findAccountsByCurrencyAndNotSenderIdAndClientId(String currency, Long senderId, Long clientId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE currency = ? AND id != ? AND client_id = ?";

        try (PreparedStatement stmt = transactionManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, currency);
            stmt.setLong(2, senderId);
            stmt.setLong(3, clientId);
            ResultSet rs = stmt.executeQuery();

            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(mapAccount(rs));
            }
            return accounts;
        }
    }

    public boolean isAccountNumberExists(String accountNumber, Long accountId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM accounts WHERE account_number = ?";

        if (accountId != null) {
            sql += " AND id != ?";
        }

        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, accountNumber);

            if (accountId != null) {
                statement.setLong(2, accountId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private String mapSortColumn(String gridColumn) {
        return switch (gridColumn) {
            case "balance" -> "balance";
            case "status" -> "status";
            case "bik" -> "bik";
            case "currency" -> "currency";
            default -> "account_number";
        };
    }

    private Account mapAccount(ResultSet resultSet) throws SQLException {
        Account account = new Account();
        account.setId(resultSet.getLong("id"));
        account.setAccountNumber(resultSet.getString("account_number"));
        account.setBalance(resultSet.getBigDecimal("balance"));
        account.setStatus(AccountStatus.valueOf(resultSet.getString("status")));
        account.setBik(resultSet.getString("bik"));
        account.setCurrency(Currency.valueOf(resultSet.getString("currency")));
        account.setClientId(resultSet.getLong("client_id"));
        account.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        account.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        return account;
    }
}