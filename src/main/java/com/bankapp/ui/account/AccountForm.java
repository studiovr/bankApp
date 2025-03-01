package com.bankapp.ui.account;

import com.bankapp.enums.Currency;
import com.bankapp.service.AccountService;
import com.bankapp.service.ClientService;
import com.bankapp.utils.DIContainer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bankapp.model.Account;
import com.bankapp.model.Client;

import java.sql.SQLException;

@Route("account-form")
public class AccountForm extends VerticalLayout {

    private final AccountService accountService;
    private final ClientService clientService;
    private static final Logger logger = LoggerFactory.getLogger(AccountForm.class);
    private final Binder<Account> binder = new Binder<>(Account.class);

    private final TextField accountNumber = new TextField("Номер счета");
    private final ComboBox<Currency> currency = new ComboBox<>("Валюта");
    private final TextField bik = new TextField("БИК");
    private final ComboBox<Client> client = new ComboBox<>("Клиент");

    public AccountForm() {
        this.accountService = DIContainer.get(AccountService.class);
        this.clientService = DIContainer.get(ClientService.class);
        setAlignItems(Alignment.CENTER);
        initForm();
    }

    private void initForm() {
        addClassName("deposit-form");
        var title = new com.vaadin.flow.component.html.Paragraph("Форма создания счета");
        title.getStyle()
                .set("font-size", "20px")
                .set("font-weight", "bold")
                .set("margin-bottom", "15px");
        add(title);

        initFields();
        initValidation();

        FormLayout formLayout = new FormLayout();
        formLayout.add(accountNumber, currency, bik, client);

        Button saveButton = new Button("Сохранить", event -> saveAccount());
        Button cancelButton = new Button("Отмена", event -> getUI().ifPresent(ui -> ui.navigate("")));

        VerticalLayout buttonsLayout = new VerticalLayout();
        buttonsLayout.setWidthFull();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setPadding(true);
        buttonsLayout.setAlignItems(Alignment.CENTER);

        saveButton.setWidth("200px");
        cancelButton.setWidth("200px");

        buttonsLayout.add(saveButton, cancelButton);
        add(formLayout, buttonsLayout);

        binder.bindInstanceFields(this);
    }

    private void initFields() {
        try {
            client.setItems(clientService.findAllClients());
            client.setItemLabelGenerator(Client::getFullName);
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке списка клиентов: {}", e.getMessage(), e);
            Notification.show("Ошибка при загрузке клиентов: " + e.getMessage());
        }
        currency.setItems(Currency.values());
        currency.setItemLabelGenerator(Currency::getDescription);
    }

    private void initValidation() {
        binder.forField(accountNumber)
                .asRequired("Номер счета обязателен")
                .withValidator(new StringLengthValidator("Номер счета должен содержать минимум 3 символа", 3, null))
                .bind(Account::getAccountNumber, Account::setAccountNumber);

        binder.forField(currency)
                .asRequired("Валюта обязательна")
                .bind(Account::getCurrency, Account::setCurrency);

        binder.forField(bik)
                .asRequired("БИК обязателен")
                .withValidator(new RegexpValidator("БИК должен состоять из 9 цифр", "^\\d{9}$"))
                .bind(Account::getBik, Account::setBik);

        binder.forField(client)
                .asRequired("Клиент обязателен")
                .bind(account -> null, (account, selectedClient) -> account.setClientId(selectedClient.getId()));
    }

    private void saveAccount() {
        Account account = new Account();
        if (binder.writeBeanIfValid(account)) {
            try {
                logger.info("Сохранение нового счета для клиента с id={}", account.getClientId());

                accountService.createAccount(account);

                logger.info("Счет успешно сохранен, номер счета={}", account.getAccountNumber());

                Notification.show("Счет успешно сохранен");
                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (Exception e) {
                logger.error("Ошибка при создании счета: {}", e.getMessage(), e);
                Notification.show("Ошибка: " + e.getMessage());
            }
        } else {
            Notification.show("Пожалуйста, заполните все поля корректно");
        }
    }
}