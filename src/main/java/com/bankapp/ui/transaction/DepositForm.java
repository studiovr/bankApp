package com.bankapp.ui.transaction;

import com.bankapp.service.AccountService;
import com.bankapp.utils.DIContainer;
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
import java.sql.SQLException;

@Route("deposit-form")
public class DepositForm extends VerticalLayout {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(DepositForm.class);

    private ComboBox<Account> account = new ComboBox<>("Счет");
    private final BigDecimalField amount = new BigDecimalField("Сумма");
    private TextField currencyField = new TextField("Валюта");

    public DepositForm() {
        this.accountService = DIContainer.get(AccountService.class);
        this.transactionService = DIContainer.get(TransactionService.class);
        initForm();
    }

    private void initForm() {
        setAlignItems(Alignment.CENTER);

        addClassName("deposit-form");
        var title = new com.vaadin.flow.component.html.Paragraph("Форма зачисления средств");
        title.getStyle()
                .set("font-size", "20px")
                .set("font-weight", "bold")
                .set("margin-bottom", "15px");

        add(title);

        loadAccounts();

        currencyField.setReadOnly(true);
        currencyField.setWidth("100px");
        account.setPlaceholder("Выберите счет");
        amount.setPlaceholder("Введите сумму");

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

        Button depositButton = new Button("Зачислить", event -> depositFunds());
        Button cancelButton = new Button("Отмена", event -> getUI().ifPresent(ui -> ui.navigate("")));

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
                Notification.show("Сумма перевода должна быть больше нуля");
                return;
            }

            logger.info("Начинается зачисление средств на счет {} на сумму {}", selectedAccount.getAccountNumber(), depositAmount);

            try {
                accountService.depositFunds(selectedAccount.getId(), depositAmount);

                logger.info("Средства успешно зачислены на счет {}", selectedAccount.getAccountNumber());
                Notification.show("Средства успешно зачислены");

                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (Exception e) {
                Notification.show("Ошибка при зачислении средств: " + e.getMessage());
            }
        } else {
            Notification.show("Пожалуйста, заполните все поля корректно");
        }
    }

    private void loadAccounts() {
        try {
            account.setItems(accountService.findAllAccounts());
            account.setItemLabelGenerator(Account::getAccountNumber);
        } catch (Exception e) {
            logger.error("Ошибка при загрузке счетов", e);
            Notification.show("Не удалось загрузить счета");
        }
    }
}