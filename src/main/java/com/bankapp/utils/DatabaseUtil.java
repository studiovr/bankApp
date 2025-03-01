package com.bankapp.utils;

import java.sql.*;

public class DatabaseUtil {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/bank";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public static void initializeDatabase() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Создание таблицы clients
            String createClientsTable = "CREATE TABLE IF NOT EXISTS clients ("
                    + "id SERIAL PRIMARY KEY, "
                    + "full_name VARCHAR(255) NOT NULL, "
                    + "phone_number VARCHAR(20) UNIQUE NOT NULL, "
                    + "inn VARCHAR(20) NOT NULL, "
                    + "address VARCHAR(255) NOT NULL, "
                    + "passport_scan_copy BYTEA, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP "
                    + ")";
            statement.execute(createClientsTable);

            // Создание таблицы accounts
            String createAccountsTable = "CREATE TABLE IF NOT EXISTS accounts ("
                    + "id SERIAL PRIMARY KEY, "
                    + "account_number VARCHAR(20) UNIQUE NOT NULL, "
                    + "balance DECIMAL(15, 2) NOT NULL, "
                    + "status VARCHAR(10) CHECK (status IN ('OPEN', 'CLOSED')) NOT NULL, "
                    + "bik VARCHAR(20) NOT NULL, "
                    + "currency VARCHAR(3) CHECK (currency IN ('RUB', 'USD', 'EUR')) NOT NULL, "
                    + "client_id INT REFERENCES clients(id) ON DELETE CASCADE, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP "
                    + ")";
            statement.execute(createAccountsTable);

            // Создание таблицы transactions
            String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
                    + "id SERIAL PRIMARY KEY, "
                    + "from_account_id INT REFERENCES accounts(id) ON DELETE CASCADE, "
                    + "to_account_id INT REFERENCES accounts(id) ON DELETE CASCADE, "
                    + "amount DECIMAL(15, 2) NOT NULL, "
                    + "currency VARCHAR(3) CHECK (currency IN ('RUB', 'USD', 'EUR')) NOT NULL, "
                    + "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "type VARCHAR(10) CHECK (type IN ('TRANSFER', 'CREDIT')) NOT NULL "
                    + ")";
            statement.execute(createTransactionsTable);

            // Проверка наличия функции триггера для accounts
            if (!isTriggerExists(statement, "update_accounts_updated_at")) {
                // Создание функции для триггера, если она не существует
                String createTriggerFunction = "CREATE OR REPLACE FUNCTION update_updated_at() "
                        + "RETURNS TRIGGER AS $$ "
                        + "BEGIN "
                        + "NEW.updated_at = CURRENT_TIMESTAMP; "
                        + "RETURN NEW; "
                        + "END; "
                        + "$$ LANGUAGE plpgsql;";
                statement.execute(createTriggerFunction);
            }

            // Создание триггера для таблицы accounts
            if (!isTriggerExists(statement, "update_accounts_updated_at")) {
                String createTrigger = "CREATE TRIGGER update_accounts_updated_at "
                        + "BEFORE UPDATE ON accounts "
                        + "FOR EACH ROW "
                        + "EXECUTE FUNCTION update_updated_at();";
                statement.execute(createTrigger);
            }

            // Проверка наличия функции триггера для clients
            if (!isTriggerExists(statement, "update_clients_updated_at")) {
                // Создание функции для триггера на clients, если она не существует
                String createTriggerFunctionClients = "CREATE OR REPLACE FUNCTION update_clients_updated_at() "
                        + "RETURNS TRIGGER AS $$ "
                        + "BEGIN "
                        + "NEW.updated_at = CURRENT_TIMESTAMP; "
                        + "RETURN NEW; "
                        + "END; "
                        + "$$ LANGUAGE plpgsql;";
                statement.execute(createTriggerFunctionClients);
            }

            // Создание триггера для таблицы clients
            if (!isTriggerExists(statement, "update_clients_updated_at")) {
                String createTriggerClients = "CREATE TRIGGER update_clients_updated_at "
                        + "BEFORE UPDATE ON clients "
                        + "FOR EACH ROW "
                        + "EXECUTE FUNCTION update_clients_updated_at();";
                statement.execute(createTriggerClients);
            }

            System.out.println("База данных успешно инициализирована.");
        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации базы данных: " + e.getMessage());
        }
    }

    private static boolean isTriggerExists(Statement statement, String triggerName) throws SQLException {
        String query = "SELECT COUNT(*) FROM pg_trigger WHERE tgname = '" + triggerName + "';";
        try (ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}