package com.bankapp.repository;

import com.bankapp.utils.TransactionManager;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.bankapp.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientRepository {

    private final TransactionManager transactionManager;

    public ClientRepository(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void save(Client client) throws SQLException {
        String sql = "INSERT INTO clients (full_name, phone_number, inn, address, passport_scan_copy) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, client.getFullName());
            statement.setString(2, client.getPhoneNumber());
            statement.setString(3, client.getInn());
            statement.setString(4, client.getAddress());
            statement.setBytes(5, client.getPassportScanCopy());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    client.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public boolean hasClients() throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM clients)";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getBoolean(1);
            }
        }
        return false;
    }

    public Client findById(Long id) throws SQLException {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapClient(resultSet);
                }
            }
        }
        return null;
    }

    public List<Client> findAll() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                clients.add(mapClient(resultSet));
            }
        }
        return clients;
    }

    public void update(Client client) throws SQLException {
        if (isPhoneNumberExists(client.getPhoneNumber(), client.getId())) {
            throw new SQLException("Номер телефона уже существует");
        }


        String sql = "UPDATE clients SET full_name = ?, phone_number = ?, inn = ?, address = ?, passport_scan_copy = ? WHERE id = ?";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, client.getFullName());
            statement.setString(2, client.getPhoneNumber());
            statement.setString(3, client.getInn());
            statement.setString(4, client.getAddress());
            statement.setBytes(5, client.getPassportScanCopy());
            statement.setLong(6, client.getId());
            statement.executeUpdate();
        }
    }

    public List<Client> findClients(int offset, int limit, List<QuerySortOrder> sortOrders) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM clients");

        if (!sortOrders.isEmpty()) {
            sql.append(" ORDER BY ");
            for (QuerySortOrder sortOrder : sortOrders) {
                String dbColumn = mapGridColumnToDbField(sortOrder.getSorted());
                String direction = sortOrder.getDirection().equals(SortDirection.ASCENDING) ? "ASC" : "DESC";
                sql.append(dbColumn).append(" ").append(direction).append(", ");
            }
            sql.setLength(sql.length() - 2);
        } else {
            sql.append(" ORDER BY id ASC");
        }

        sql.append(" LIMIT ? OFFSET ?");

        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql.toString())) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            ResultSet rs = statement.executeQuery();

            List<Client> clients = new ArrayList<>();
            while (rs.next()) {
                clients.add(mapClient(rs));
            }
            return clients;
        }
    }

    public int countClients() throws SQLException {
        String sql = "SELECT COUNT(*) FROM clients";
        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }

    public boolean isPhoneNumberExists(String phoneNumber, Long clientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clients WHERE phone_number = ?";

        if (clientId != null) {
            sql += " AND id != ?";
        }

        try (PreparedStatement statement = transactionManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, phoneNumber);
            if (clientId != null) {
                statement.setLong(2, clientId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private String mapGridColumnToDbField(String gridColumn) {
        return switch (gridColumn) {
            case "fullName" -> "full_name";
            case "phoneNumber" -> "phone_number";
            case "inn" -> "inn";
            case "address" -> "address";
            default -> "id";
        };
    }

    private Client mapClient(ResultSet resultSet) throws SQLException {
        Client client = new Client();
        client.setId(resultSet.getLong("id"));
        client.setFullName(resultSet.getString("full_name"));
        client.setPhoneNumber(resultSet.getString("phone_number"));
        client.setInn(resultSet.getString("inn"));
        client.setAddress(resultSet.getString("address"));
        client.setPassportScanCopy(resultSet.getBytes("passport_scan_copy"));
        client.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        client.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        return client;
    }
}