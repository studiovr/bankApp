package com.bankapp.ui;

import com.bankapp.exception.DataAccessException;
import com.bankapp.ui.components.StyledParagraph;
import com.bankapp.utils.MessageProvider;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bankapp.service.AccountService;
import com.bankapp.service.ClientService;
import com.bankapp.utils.ServiceLocator;

import java.sql.SQLException;

@Route("")
public class MainView extends VerticalLayout {
    private final ClientService clientService;
    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);

    public MainView() {
        this.clientService = ServiceLocator.get(ClientService.class);
        this.accountService = ServiceLocator.get(AccountService.class);
        initMenu();
    }

    private void initMenu() {
        setAlignItems(Alignment.CENTER);

        StyledParagraph title = new StyledParagraph(MessageProvider.getMessage("main.title"));

        add(
                title,
                createStyledButton(MessageProvider.getMessage("button.createClient"), event -> getUI().ifPresent(ui -> ui.navigate("client-form"))),
                createStyledButton(MessageProvider.getMessage("button.createAccount"), event -> openAccountForm()),
                createStyledButton(MessageProvider.getMessage("button.closeAccount"), event -> openForm("close-account-form")),
                createStyledButton(MessageProvider.getMessage("button.transferFunds"), event -> openForm("transfer-form")),
                createStyledButton(MessageProvider.getMessage("button.depositFunds"), event -> openForm("deposit-form")),
                createStyledButton(MessageProvider.getMessage("button.clientList"), event -> getUI().ifPresent(ui -> ui.navigate("client-list"))),
                createStyledButton(MessageProvider.getMessage("button.accountList"), event -> openForm("account-list")),
                createStyledButton(MessageProvider.getMessage("button.transactionList"), event -> openForm("transaction-list"))
        );
    }

    private void openForm(String route) {
        if (checkOpenAccounts()) {
            getUI().ifPresent(ui -> ui.navigate(route));
        }
    }

    private void openAccountForm() {
        try {
            if (!clientService.hasClients()) {
                Notification.show(MessageProvider.getMessage("error.noClients"));
            } else {
                getUI().ifPresent(ui -> ui.navigate("account-form"));
            }
        } catch (SQLException e) {
            logger.error(MessageProvider.getMessage("error.clientCheck"), e);
            Notification.show(MessageProvider.getMessage("error.clientCheck") + ": " + e.getMessage());
        }
    }

    private boolean checkOpenAccounts() {
        try {
            if (!accountService.hasOpenAccounts()) {
                Notification.show(MessageProvider.getMessage("error.noOpenAccounts"));
                return false;
            }
            return true;
        } catch (DataAccessException e) {
            logger.error(MessageProvider.getMessage("error.accountCheck"), e);
            Notification.show(MessageProvider.getMessage("error.accountCheck"));
            return false;
        }
    }

    private Button createStyledButton(String text, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(text, listener);
        button.setWidth("250px");
        return button;
    }
}
