package com.bankapp.ui.account;

import com.bankapp.exception.DataAccessException;
import com.bankapp.ui.components.StyledParagraph;
import com.bankapp.utils.MessageProvider;
import com.vaadin.flow.component.textfield.BigDecimalField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bankapp.utils.ServiceLocator;
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
import java.util.List;

@Route("close-account-form")
public class CloseAccountForm extends VerticalLayout {

    private final AccountService accountService;
    private final ClientService clientService;
    private static final Logger logger = LoggerFactory.getLogger(CloseAccountForm.class);

    private final ComboBox<Client> clientComboBox = new ComboBox<>(MessageProvider.getMessage("account.client"));
    private final ComboBox<Account> accountComboBox = new ComboBox<>(MessageProvider.getMessage("account.number"));

    public CloseAccountForm() {
        this.accountService = ServiceLocator.get(AccountService.class);
        this.clientService = ServiceLocator.get(ClientService.class);
        setAlignItems(Alignment.CENTER);
        initForm();
    }

    private void initForm() {
        StyledParagraph title = new StyledParagraph(MessageProvider.getMessage("form.title.closeAccount"));

        add(title);


        configureClientComboBox();
        configureAccountComboBox();

        Button closeButton = new Button(MessageProvider.getMessage("button.closeAccount"), event -> closeAccount());
        Button cancelButton = new Button(MessageProvider.getMessage("button.cancel"), event -> getUI().
                ifPresent(ui -> ui.navigate("")));

        closeButton.setWidth("200px");
        cancelButton.setWidth("200px");

        VerticalLayout buttonsLayout = new VerticalLayout(closeButton, cancelButton);
        buttonsLayout.setAlignItems(Alignment.CENTER);
        buttonsLayout.setSpacing(true);

        add(clientComboBox, accountComboBox, buttonsLayout);
    }

    private void configureClientComboBox() {
        try {
            clientComboBox.setItems(clientService.findAllClients());
        } catch (DataAccessException e) {
            Notification.show(MessageProvider.getMessage("error.loadClients"));
        }

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
                } catch (DataAccessException e) {
                    Notification.show(MessageProvider.getMessage("notification.loadAccountsError"));
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
                    Notification.show(MessageProvider.getMessage("notification.accountClosed"));
                    getUI().ifPresent(ui -> ui.navigate(""));
                }
            } catch (DataAccessException e) {
                Notification.show(MessageProvider.getMessage("notification.transferError"));
            }
        } else {
            Notification.show(MessageProvider.getMessage("notification.selectAccount"));
        }
    }

    private void showTransferDialog(Account account) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(MessageProvider.getMessage("dialog.transferTitle"));

        BigDecimalField amountField = new BigDecimalField(MessageProvider.getMessage("dialog.transferAmount"));
        amountField.setValue(account.getBalance());
        amountField.setReadOnly(true);

        ComboBox<Account> targetAccountComboBox = new ComboBox<>(MessageProvider.getMessage("dialog.transferTargetAccount"));

        try {
            List<Account> clientAccounts = accountService.findAccountsByCurrencyAndNotSenderIdAndClientId(account.getCurrency().toString(),
                    account.getId(), account.getClientId());

            if (clientAccounts.isEmpty()) {
                List<Account> allAccounts = accountService.findAccountsByCurrencyAndNotSenderId(account.getCurrency().toString(),
                        account.getId());

                if (allAccounts.isEmpty()) {
                    Notification.show(MessageProvider.getMessage("notification.noAccountsForTransfer"));
                    return;
                }

                targetAccountComboBox.setItems(allAccounts);
                Notification.show(MessageProvider.getMessage("notification.noAccountsForTransfer"));
            } else {
                targetAccountComboBox.setItems(clientAccounts);
            }

            targetAccountComboBox.setItemLabelGenerator(acc ->
                    String.format("%s (Клиент: %s)", acc.getAccountNumber(), acc.getClientId())
            );
        } catch (DataAccessException e) {
            Notification.show(MessageProvider.getMessage("notification.loadAccountsError"));
            return;
        }

        Button transferButton = new Button(MessageProvider.getMessage("button.transferAndClose"), event -> {
            Account targetAccount = targetAccountComboBox.getValue();
            if (targetAccount != null) {
                try {
                    accountService.transferFunds(account.getId(), targetAccount.getId(), account.getBalance(), account.getCurrency());
                    accountService.closeAccount(account.getId());

                    logger.info("Cчет закрыт");
                    Notification.show(MessageProvider.getMessage("notification.transferSuccess"));

                    dialog.close();
                    getUI().ifPresent(ui -> ui.navigate(""));
                } catch (DataAccessException e) {
                    Notification.show(MessageProvider.getMessage("notification.transferError"));
                }
            } else {
                Notification.show(MessageProvider.getMessage("notification.selectAccount"));
            }
        });

        Button cancelButton = new Button(MessageProvider.getMessage("button.cancel"), event -> dialog.close());

        dialog.add(amountField, targetAccountComboBox, transferButton, cancelButton);
        dialog.open();
    }
}