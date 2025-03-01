package com.bankapp.ui;

import com.bankapp.utils.TransactionManager;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.bankapp.repository.AccountRepositoryImpl;
import com.bankapp.repository.ClientRepositoryImpl;
import com.bankapp.repository.TransactionRepositoryImpl;
import com.bankapp.service.AccountService;
import com.bankapp.service.ClientService;
import com.bankapp.service.TransactionService;
import com.bankapp.utils.ServiceLocator;
import com.bankapp.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@PWA(name = "Project Base for Vaadin", shortName = "Project Base")
@Theme("my-theme")
public class AppShell implements AppShellConfigurator {
    static {
        try {
            DatabaseUtil.initializeDatabase();
            Connection connection = DatabaseUtil.getConnection();

            TransactionManager transactionManager = new TransactionManager(connection);
            ServiceLocator.register(TransactionManager.class, transactionManager);

            ClientRepositoryImpl clientRepositoryImpl = new ClientRepositoryImpl(transactionManager);
            AccountRepositoryImpl accountRepositoryImpl = new AccountRepositoryImpl(transactionManager);
            TransactionRepositoryImpl transactionRepositoryImpl = new TransactionRepositoryImpl(transactionManager);

            ServiceLocator.register(ClientRepositoryImpl.class, clientRepositoryImpl);
            ServiceLocator.register(AccountRepositoryImpl.class, accountRepositoryImpl);
            ServiceLocator.register(TransactionRepositoryImpl.class, transactionRepositoryImpl);

            ClientService clientService = new ClientService(clientRepositoryImpl);
            TransactionService transactionService = new TransactionService(transactionRepositoryImpl);
            AccountService accountService = new AccountService(accountRepositoryImpl, transactionManager,
                    transactionService);

            ServiceLocator.register(ClientService.class, clientService);
            ServiceLocator.register(AccountService.class, accountService);
            ServiceLocator.register(TransactionService.class, transactionService);

        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации: " + e.getMessage());
        }
    }
}
