package com.bankapp.ui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bankapp.service.AccountService;
import com.bankapp.service.ClientService;
import com.bankapp.utils.DIContainer;

import java.sql.SQLException;

@Route("")
public class MainView extends VerticalLayout {
    private final ClientService clientService;
    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);

    public MainView() {
        this.clientService = DIContainer.get(ClientService.class);
        this.accountService = DIContainer.get(AccountService.class);
        initMenu();
    }

    private void initMenu() {
        setAlignItems(Alignment.CENTER);

        Paragraph title = new Paragraph("Выберите действие");
        title.getStyle().set("font-size", "20px").set("font-weight", "bold").set("margin-bottom", "15px");

        add(
                title,
                createStyledButton("Создать клиента", event -> getUI().ifPresent(ui -> ui.navigate("client-form"))),
                createStyledButton("Создать счет", event -> openAccountForm()),
                createStyledButton("Закрыть счет", event -> openForm("close-account-form")),
                createStyledButton("Перевести средства", event -> openForm("transfer-form")),
                createStyledButton("Зачислить средства", event -> openForm("deposit-form")),
                createStyledButton("Список клиентов", event -> getUI().ifPresent(ui -> ui.navigate("client-list"))),
                createStyledButton("Список счетов", event -> openForm("account-list")),
                createStyledButton("Список транзакций", event -> openForm("transaction-list"))
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
                Notification.show("Клиентов не найдено. Сначала создайте клиента.");
            } else {
                getUI().ifPresent(ui -> ui.navigate("account-form"));
            }
        } catch (SQLException e) {
            logger.error("Ошибка при проверке клиентов", e);
            Notification.show("Ошибка при проверке клиентов: " + e.getMessage());
        }
    }

    private boolean checkOpenAccounts() {
        try {
            if (!accountService.hasOpenAccounts()) {
                Notification.show("Открытых счетов не найдено.");
                return false;
            }
            return true;
        } catch (SQLException e) {
            logger.error("Ошибка при проверке счетов", e);
            Notification.show("Ошибка при проверке счетов: " + e.getMessage());
            return false;
        }
    }

    private Button createStyledButton(String text, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(text, listener);
        button.setWidth("250px");
        return button;
    }
}
