package com.bankapp.ui.transaction;

import com.bankapp.enums.Currency;
import com.bankapp.exception.DataAccessException;
import com.bankapp.service.AccountService;
import com.bankapp.ui.components.StyledParagraph;
import com.bankapp.utils.MessageProvider;
import com.bankapp.utils.ServiceLocator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bankapp.model.Account;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Route("transfer-form")
public class TransferForm extends VerticalLayout {

    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(TransferForm.class);

    private final ComboBox<Account> fromAccount = new ComboBox<>(MessageProvider.getMessage("transfer.fromAccount"));
    private final ComboBox<Account> toAccount = new ComboBox<>(MessageProvider.getMessage("transfer.toAccount"));
    private final BigDecimalField amount = new BigDecimalField(MessageProvider.getMessage("dialog.transferAmount"));
    private final TextField currencyField = new TextField(MessageProvider.getMessage("account.currency"));

    public TransferForm() {
        this.accountService = ServiceLocator.get(AccountService.class);
        initForm();
    }

    private void initForm() {
        setAlignItems(Alignment.CENTER);

        StyledParagraph title = new StyledParagraph(MessageProvider.getMessage("form.title.transfer"));

        add(title);

        try {
            fromAccount.setItems(accountService.findAllAccounts());
            fromAccount.setItemLabelGenerator(Account::getAccountNumber);
            toAccount.setItems(accountService.findAllAccounts());
            toAccount.setItemLabelGenerator(Account::getAccountNumber);
        } catch (DataAccessException e) {
            Notification.show(MessageProvider.getMessage("notification.loadAccountsError"));
        }

        fromAccount.setWidth("300px");
        toAccount.setWidth("300px");
        amount.setWidth("200px");
        currencyField.setWidth("100px");
        currencyField.setReadOnly(true);

        fromAccount.setPlaceholder(MessageProvider.getMessage("transfer.selectFromAccount"));
        toAccount.setPlaceholder(MessageProvider.getMessage("transfer.selectToAccount"));
        amount.setPlaceholder(MessageProvider.getMessage("dialog.transferAmount"));

        HorizontalLayout amountLayout = new HorizontalLayout(amount, currencyField);
        amountLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        fromAccount.addValueChangeListener(event -> {
            Account selectedAccount = event.getValue();
            if (selectedAccount != null) {
                Currency currency = selectedAccount.getCurrency();
                currencyField.setValue(currency.toString());
                try {
                    List<Account> availableAccounts = accountService.findAccountsByCurrencyAndNotSenderId(currency.toString(), selectedAccount.getId());
                    toAccount.setItems(availableAccounts);

                    if (availableAccounts.isEmpty()) {
                        Notification.show(MessageProvider.getMessage("notification.noActiveAccounts" + currency));
                    }
                } catch (DataAccessException e) {
                    Notification.show(MessageProvider.getMessage("notification.loadAccountsError"));
                }
            } else {
                currencyField.clear();
                toAccount.setItems();
            }
        });

        Button transferButton = new Button(MessageProvider.getMessage("button.transferFunds"), event -> transferFunds());
        Button cancelButton = new Button(MessageProvider.getMessage("button.cancel"), event -> getUI()
                .ifPresent(ui -> ui.navigate("")));

        transferButton.setWidth("200px");
        cancelButton.setWidth("200px");

        VerticalLayout buttonsLayout = new VerticalLayout(transferButton, cancelButton);
        buttonsLayout.setAlignItems(Alignment.CENTER);
        buttonsLayout.setSpacing(true);

        add(fromAccount, toAccount, amountLayout, buttonsLayout);
    }


    private void transferFunds() {
        Account from = fromAccount.getValue();
        Account to = toAccount.getValue();
        BigDecimal transferAmount = amount.getValue();

        if (from != null && to != null) {
            if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
                Notification.show(MessageProvider.getMessage("notification.amountGreaterThanZero"));
                return;
            }

            if (transferAmount.compareTo(from.getBalance()) > 0) {
                Notification.show(MessageProvider.getMessage("notification.insufficientFunds"));
                return;
            }

            logger.info("Начинаем перевод {} из счета {} на счет {} на сумму {}", transferAmount, from.getAccountNumber(), to.getAccountNumber(), transferAmount);


            try {
                accountService.transferFunds(from.getId(), to.getId(), transferAmount, from.getCurrency());
                Notification.show(MessageProvider.getMessage("notification.transferCompleted"));
            } catch (DataAccessException e) {
                Notification.show(MessageProvider.getMessage("notification.transferFailed"));
            }
            getUI().ifPresent(ui -> ui.navigate(""));

        } else {
            Notification.show(MessageProvider.getMessage("notification.fillFieldsCorrectly"));
        }
    }
}