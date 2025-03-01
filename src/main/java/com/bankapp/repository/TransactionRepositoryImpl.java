package com.bankapp.repository;

import com.bankapp.enums.Currency;
import com.bankapp.enums.TransactionType;
import com.bankapp.model.Transaction;
import com.bankapp.utils.TransactionManager;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepositoryImpl extends AbstractRepository<Transaction, Long> {

    public TransactionRepositoryImpl(TransactionManager transactionManager) {
        super(transactionManager);
    }

    @Override
    public void save(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (from_account_id, to_account_id, amount, currency, type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Long fromAccountId = transaction.getFromAccount();
            if (fromAccountId != null) {
                statement.setLong(1, fromAccountId);
            } else {
                statement.setNull(1, Types.BIGINT);
            }
            statement.setLong(2, transaction.getToAccount());
            statement.setBigDecimal(3, transaction.getAmount());
            statement.setString(4, transaction.getCurrency().toString());
            statement.setString(5, transaction.getType().toString());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    @Override
    public List<Transaction> findAll() throws SQLException {
        String sql = "SELECT * FROM transactions";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Transaction> transactions = new ArrayList<>();
            while (resultSet.next()) {
                transactions.add(mapTransaction(resultSet));
            }
            return transactions;
        }
    }
    public List<Transaction> findTransactions(int offset, int limit, List<QuerySortOrder> sortOrders) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions");

        if (!sortOrders.isEmpty()) {
            sql.append(" ORDER BY ");
            for (QuerySortOrder sortOrder : sortOrders) {
                sql.append(mapSortColumn(sortOrder.getSorted()))
                        .append(sortOrder.getDirection().equals(SortDirection.ASCENDING) ? " ASC" : " DESC")
                        .append(", ");
            }
            sql.setLength(sql.length() - 2);
        } else {
            sql.append(" ORDER BY transaction_date DESC");
        }

        sql.append(" LIMIT ? OFFSET ?");

        try (PreparedStatement stmt = transactionManager.getConnection().prepareStatement(sql.toString())) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            List<Transaction> transactions = new ArrayList<>();
            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }
            return transactions;
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTransaction(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }

    private Transaction mapTransaction(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(resultSet.getLong("id"));
        transaction.setFromAccount(resultSet.getLong("from_account_id"));
        transaction.setToAccount(resultSet.getLong("to_account_id"));
        transaction.setAmount(resultSet.getBigDecimal("amount"));
        transaction.setCurrency(Currency.valueOf(resultSet.getString("currency")));
        transaction.setTransactionDate(resultSet.getTimestamp("transaction_date").toLocalDateTime());
        transaction.setStatus(TransactionType.valueOf(resultSet.getString("type")));
        return transaction;
    }

    private String mapSortColumn(String gridColumn) {
        return switch (gridColumn) {
            case "fromAccount" -> "from_account_id";
            case "toAccount" -> "to_account_id";
            case "amount" -> "amount";
            case "currency" -> "currency";
            case "transactionDate" -> "transaction_date";
            case "type" -> "type";
            default -> "transaction_date";
        };
    }
}
