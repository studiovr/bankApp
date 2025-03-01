package com.bankapp.ui.account;

import com.vaadin.flow.component.textfield.BigDecimalField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bankapp.service.TransactionService;
import com.bankapp.utils.DIContainer;
import com.bankapp.model.Account;
import com.bankapp.model.Client;
import com.bankapp.service.AccountService;
import com.bankapp.service.ClientService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Route("close-account-form")
public class CloseAccountForm extends VerticalLayout {

    private final AccountService accountService;
    private final ClientService clientService;
    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(CloseAccountForm.class);

    private ComboBox<Client> clientComboBox = new ComboBox<>("Клиент");
    private ComboBox<Account> accountComboBox = new ComboBox<>("Счет");

    public CloseAccountForm() {
        this.accountService = DIContainer.get(AccountService.class);
        this.clientService = DIContainer.get(ClientService.class);
        this.transactionService = DIContainer.get(TransactionService.class);
        setAlignItems(Alignment.CENTER);
        initForm();
    }

    private void initForm() {
        var title = new com.vaadin.flow.component.html.Paragraph("Форма закрытия счета");
        title.getStyle()
                .set("font-size", "20px")
                .set("font-weight", "bold")
                .set("margin-bottom", "15px");

        add(title);

        try {
            configureClientComboBox();
            configureAccountComboBox();

            Button closeButton = new Button("Закрыть счет", event -> closeAccount());
            Button cancelButton = new Button("Отмена", event -> getUI().ifPresent(ui -> ui.navigate("")));

            closeButton.setWidth("200px");
            cancelButton.setWidth("200px");

            VerticalLayout buttonsLayout = new VerticalLayout(closeButton, cancelButton);
            buttonsLayout.setAlignItems(Alignment.CENTER);
            buttonsLayout.setSpacing(true);

            add(clientComboBox, accountComboBox, buttonsLayout);
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке списка клиентов", e);
            Notification.show("Ошибка при загрузке клиентов: " + e.getMessage());
        }
    }

    private void configureClientComboBox() throws SQLException {
        clientComboBox.setItems(clientService.findAllClients());
        clientComboBox.setItemLabelGenerator(Client::getFullName);
        clientComboBox.setWidth("300px");
    }

    private void configureAccountComboBox() {
        accountComboBox.setWidth("300px");

        clientComboBox.addValueChangeListener(event -> {
            Client selectedClient = event.getValue();
            if (selectedClient != null) {
                try {
                    accountComboBox.setItems(accountService.findAccountsByClientId(selectedClient.getId()));
                    accountComboBox.setItemLabelGenerator(Account::getAccountNumber);
                } catch (SQLException e) {
                    logger.error("Ошибка при загрузке счетов для клиента {}", selectedClient.getFullName(), e);
                    Notification.show("Ошибка при загрузке счетов: " + e.getMessage());
                }
            }
        });
    }

    private void closeAccount() {
        Account selectedAccount = accountComboBox.getValue();
        if (selectedAccount != null) {
            try {
                BigDecimal balance = selectedAccount.getBalance();
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    showTransferDialog(selectedAccount);
                } else {
                    accountService.closeAccount(selectedAccount.getId());
                    Notification.show("Счет успешно закрыт");
                    getUI().ifPresent(ui -> ui.navigate(""));
                }
            } catch (SQLException e) {
                logger.error("Ошибка при закрытии счета {}", selectedAccount.getAccountNumber(), e);
                Notification.show("Ошибка при закрытии счета: " + e.getMessage());
            }
        } else {
            Notification.show("Пожалуйста, выберите счет");
        }
    }

    private void showTransferDialog(Account account) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Перевод остатка");

        BigDecimalField amountField = new BigDecimalField("Сумма");
        amountField.setValue(account.getBalance());
        amountField.setReadOnly(true);

        ComboBox<Account> targetAccountComboBox = new ComboBox<>("Счет для перевода");

        try {
            List<Account> clientAccounts = accountService.findAccountsByCurrencyAndNotSenderIdAndClientId(account.getCurrency().toString(),
                    account.getId(), account.getClientId());

            if (clientAccounts.isEmpty()) {
                List<Account> allAccounts = accountService.findAccountsByCurrencyAndNotSenderId(account.getCurrency().toString(),
                        account.getId());

                if (allAccounts.isEmpty()) {
                    Notification.show("Нет доступных счетов для перевода остатка");
                    return;
                }

                targetAccountComboBox.setItems(allAccounts);
                Notification.show("У клиента нет других счетов. Выберите счет другого клиента.");
            } else {
                targetAccountComboBox.setItems(clientAccounts);
            }

            targetAccountComboBox.setItemLabelGenerator(acc ->
                    String.format("%s (Клиент: %s)", acc.getAccountNumber(), acc.getClientId())
            );
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке счетов для перевода", e);
            Notification.show("Ошибка при загрузке счетов: " + e.getMessage());
            return;
        }

        Button transferButton = new Button("Перевести и закрыть", event -> {
            Account targetAccount = targetAccountComboBox.getValue();
            if (targetAccount != null) {
                try {
                    accountService.transferFunds(account.getId(), targetAccount.getId(), account.getBalance(), account.getCurrency());
                    accountService.closeAccount(account.getId());

                    logger.info("Cчет закрыт");
                    Notification.show("Остаток переведен, счет закрыт");

                    dialog.close();
                    getUI().ifPresent(ui -> ui.navigate(""));
                } catch (SQLException e) {
                    Notification.show("Ошибка при переводе средств: " + e.getMessage());
                }
            } else {
                Notification.show("Пожалуйста, выберите счет для перевода");
            }
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());

        dialog.add(amountField, targetAccountComboBox, transferButton, cancelButton);
        dialog.open();
    }
}