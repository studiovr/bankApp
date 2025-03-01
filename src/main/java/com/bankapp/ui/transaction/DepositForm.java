package com.bankapp.ui.transaction;

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
import com.bankapp.service.TransactionService;

import java.math.BigDecimal;

@Route("deposit-form")
public class DepositForm extends VerticalLayout {

    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(DepositForm.class);

    private ComboBox<Account> account = new ComboBox<>(MessageProvider.getMessage("account.number"));
    private final BigDecimalField amount = new BigDecimalField(MessageProvider.getMessage("dialog.transferAmount"));
    private TextField currencyField = new TextField(MessageProvider.getMessage("account.currency"));

    public DepositForm() {
        this.accountService = ServiceLocator.get(AccountService.class);
        initForm();
    }

    private void initForm() {
        setAlignItems(Alignment.CENTER);

        addClassName("deposit-form");

        StyledParagraph title = new StyledParagraph(MessageProvider.getMessage("form.title.deposit"));

        add(title);

        loadAccounts();

        currencyField.setReadOnly(true);
        currencyField.setWidth("100px");
        account.setPlaceholder(MessageProvider.getMessage("account.selectAccount"));
        amount.setPlaceholder(MessageProvider.getMessage("dialog.transferAmount"));

        HorizontalLayout amountLayout = new HorizontalLayout(amount, currencyField);
        amountLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        account.addValueChangeListener(event -> {
            Account selectedAccount = event.getValue();
            if (selectedAccount != null) {
                currencyField.setValue(selectedAccount.getCurrency().toString());
            } else {
                currencyField.clear();
            }
        });

        Button depositButton = new Button(MessageProvider.getMessage("button.depositFunds"), event -> depositFunds());
        Button cancelButton = new Button(MessageProvider.getMessage("button.cancel"), event -> getUI().ifPresent(ui -> ui.navigate("")));

        depositButton.setWidth("200px");
        cancelButton.setWidth("200px");

        VerticalLayout buttonsLayout = new VerticalLayout(depositButton, cancelButton);
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(Alignment.CENTER);

        add(account, amountLayout, buttonsLayout);
    }

    private void depositFunds() {
        Account selectedAccount = account.getValue();
        BigDecimal depositAmount = amount.getValue();

        if (selectedAccount != null && depositAmount != null) {
            if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
                Notification.show(MessageProvider.getMessage("notification.amountGreaterThanZero"));
                return;
            }

            logger.info("Начинается зачисление средств на счет {} на сумму {}", selectedAccount.getAccountNumber(), depositAmount);

            try {
                accountService.depositFunds(selectedAccount.getId(), depositAmount);

                logger.info("Средства успешно зачислены на счет {}", selectedAccount.getAccountNumber());
                Notification.show(MessageProvider.getMessage("notification.depositSuccess"));

                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (Exception e) {
                Notification.show(MessageProvider.getMessage("notification.depositError"));
            }
        } else {
            Notification.show(MessageProvider.getMessage("notification.fillFieldsCorrectly"));
        }
    }

    private void loadAccounts() {
        try {
            account.setItems(accountService.findAllAccounts());
            account.setItemLabelGenerator(Account::getAccountNumber);
        } catch (Exception e) {
            logger.error(MessageProvider.getMessage("notification.loadAccountsError"), e);
            Notification.show(MessageProvider.getMessage("notification.loadAccountsError"));
        }
    }
}