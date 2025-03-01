package com.bankapp.ui.transaction;

import com.bankapp.enums.Currency;
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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Route("transfer-form")
public class TransferForm extends VerticalLayout {

    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(TransferForm.class);

    private ComboBox<Account> fromAccount = new ComboBox<>("Счет отправителя");
    private ComboBox<Account> toAccount = new ComboBox<>("Счет получателя");
    private BigDecimalField amount = new BigDecimalField("Сумма");
    private TextField currencyField = new TextField("Валюта");

    public TransferForm() {
        this.accountService = DIContainer.get(AccountService.class);
        initForm();
    }

    private void initForm() {
        setAlignItems(Alignment.CENTER);

        var title = new com.vaadin.flow.component.html.Paragraph("Форма перевода средств");
        title.getStyle()
                .set("font-size", "20px")
                .set("font-weight", "bold")
                .set("margin-bottom", "15px");

        add(title);

        try {
            fromAccount.setItems(accountService.findAllAccounts());
            fromAccount.setItemLabelGenerator(Account::getAccountNumber);
            toAccount.setItems(accountService.findAllAccounts());
            toAccount.setItemLabelGenerator(Account::getAccountNumber);
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке счетов", e);
            Notification.show("Ошибка при загрузке счетов: " + e.getMessage());
        }

        fromAccount.setWidth("300px");
        toAccount.setWidth("300px");
        amount.setWidth("200px");
        currencyField.setWidth("100px");
        currencyField.setReadOnly(true);

        fromAccount.setPlaceholder("Выберите счет отправителя");
        toAccount.setPlaceholder("Выберите счет получателя");
        amount.setPlaceholder("Введите сумму");

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
                        Notification.show("Нет активных счетов в валюте: " + currency);
                    }
                } catch (SQLException e) {
                    logger.error("Ошибка при загрузке счетов для валюты {}", selectedAccount.getCurrency(), e);
                    Notification.show("Ошибка при загрузке счетов: " + e.getMessage());
                }
            } else {
                currencyField.clear();
                toAccount.setItems();
            }
        });

        Button transferButton = new Button("Перевести", event -> transferFunds());
        Button cancelButton = new Button("Отмена", event -> getUI().ifPresent(ui -> ui.navigate("")));

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
                Notification.show("Сумма перевода должна быть больше нуля");
                return;
            }

            if (transferAmount.compareTo(from.getBalance()) > 0) {
                Notification.show("Сумма перевода не может быть больше остатка на счете");
                return;
            }

            logger.info("Начинаем перевод {} из счета {} на счет {} на сумму {}", transferAmount, from.getAccountNumber(), to.getAccountNumber(), transferAmount);


            try {
                accountService.transferFunds(from.getId(), to.getId(), transferAmount, from.getCurrency());
                Notification.show("Перевод успешно выполнен");
            } catch (SQLException e) {
                Notification.show("Ошибка при выполнении транзакции: " + e.getMessage());
            }
            getUI().ifPresent(ui -> ui.navigate(""));

        } else {
            Notification.show("Пожалуйста, заполните все поля корректно");
        }
    }
}