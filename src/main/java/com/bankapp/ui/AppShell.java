package com.bankapp.ui;

import com.bankapp.utils.TransactionManager;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.ClientRepository;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.service.AccountService;
import com.bankapp.service.ClientService;
import com.bankapp.service.TransactionService;
import com.bankapp.utils.DIContainer;
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
            DIContainer.register(TransactionManager.class, transactionManager);

            ClientRepository clientRepository = new ClientRepository(transactionManager);
            AccountRepository accountRepository = new AccountRepository(transactionManager);
            TransactionRepository transactionRepository = new TransactionRepository(transactionManager);

            DIContainer.register(ClientRepository.class, clientRepository);
            DIContainer.register(AccountRepository.class, accountRepository);
            DIContainer.register(TransactionRepository.class, transactionRepository);

            ClientService clientService = new ClientService(clientRepository);
            TransactionService transactionService = new TransactionService(transactionRepository);
            AccountService accountService = new AccountService(accountRepository, transactionManager,
                    transactionService);

            DIContainer.register(ClientService.class, clientService);
            DIContainer.register(AccountService.class, accountService);
            DIContainer.register(TransactionService.class, transactionService);

        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации: " + e.getMessage());
        }
    }
}
