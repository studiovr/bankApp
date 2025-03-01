package com.bankapp.ui.account;

import com.bankapp.enums.AccountStatus;
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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.bankapp.model.Account;
import com.bankapp.model.Client;

import java.math.BigDecimal;
import java.sql.SQLException;

@Route("edit-account")
public class EditAccountForm extends VerticalLayout implements HasUrlParameter<Long> {

    private final AccountService accountService;
    private final ClientService clientService;
    private final Binder<Account> binder = new Binder<>(Account.class);

    private TextField idField = new TextField("ID");
    private TextField accountNumber = new TextField("Номер счета");
    private ComboBox<Currency> currency = new ComboBox<>("Валюта");
    private TextField bik = new TextField("БИК");
    private ComboBox<Client> client = new ComboBox<>("Клиент");
    private TextField balanceField = new TextField("Баланс");
    private TextField statusField = new TextField("Статус");
    private TextField createdAtField = new TextField("Дата создания");
    private TextField updatedAtField = new TextField("Дата обновления");

    private Account account;
    private boolean isEditing = false;
    private String buttonText = "Остановить редактирование";

    private Button editButton;

    public EditAccountForm() {
        this.accountService = DIContainer.get(AccountService.class);
        this.clientService = DIContainer.get(ClientService.class);
        initForm();
    }

    private void initForm() {

        setAlignItems(Alignment.CENTER);

        addClassName("deposit-form");
        var title = new com.vaadin.flow.component.html.Paragraph("Форма редактирования счета");
        title.getStyle()
                .set("font-size", "20px")
                .set("font-weight", "bold")
                .set("margin-bottom", "15px");

        add(title);

        try {
            client.setItems(clientService.findAllClients());
            client.setItemLabelGenerator(Client::getFullName);
        } catch (SQLException e) {
            Notification.show("Ошибка при загрузке клиентов: " + e.getMessage());
        }


        currency.setItems(Currency.values());
        currency.setItemLabelGenerator(Currency::name);

        idField.setReadOnly(true);
        balanceField.setReadOnly(true);
        statusField.setReadOnly(true);
        createdAtField.setReadOnly(true);
        updatedAtField.setReadOnly(true);

        FormLayout formLayout = new FormLayout();
        formLayout.add(idField, accountNumber, currency, bik, client, balanceField, statusField, createdAtField, updatedAtField);

        editButton = new Button(buttonText, event -> toggleEditingMode());
        Button saveButton = new Button("Сохранить", event -> saveAccount());
        Button cancelButton = new Button("Отмена", event -> getUI().ifPresent(ui -> ui.navigate("")));

        VerticalLayout buttonsLayout = new VerticalLayout();
        buttonsLayout.setWidthFull();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setPadding(true);

        buttonsLayout.setAlignItems(Alignment.CENTER);

        saveButton.setWidth("200px");
        cancelButton.setWidth("200px");

        buttonsLayout.add(editButton, saveButton, cancelButton);

        add(formLayout, buttonsLayout);

        binder.bindInstanceFields(this);

        binder.forField(accountNumber)
                .asRequired("Номер счета обязателен")
                .withValidator(new StringLengthValidator(
                        "Номер счета должен содержать минимум 3 символа", 3, null))
                .bind(Account::getAccountNumber, Account::setAccountNumber);

        binder.forField(currency)
                .asRequired("Валюта обязательна")
                .bind(Account::getCurrency, Account::setCurrency);

        binder.forField(bik)
                .asRequired("БИК обязателен")
                .withValidator(new RegexpValidator(
                        "БИК должен состоять из 9 цифр", "^\\d{9}$"))
                .bind(Account::getBik, Account::setBik);

        binder.forField(client)
                .asRequired("Клиент обязателен")
                .bind(account -> null, (account, selectedClient) -> account.setClientId(selectedClient.getId()));

        toggleEditingMode();
    }

    private void toggleEditingMode() {
        isEditing = !isEditing;
        accountNumber.setReadOnly(!isEditing);
        currency.setReadOnly(!isEditing);
        bik.setReadOnly(!isEditing);
        client.setReadOnly(!isEditing);

        if (isEditing) {
            buttonText = "Остановить редактирование";
        } else {
            buttonText = "Включить редактирование";
        }
        editButton.setText(buttonText);
    }

    private void saveAccount() {
        if (binder.writeBeanIfValid(account)) {
            try {
                if (account.getId() == null) {
                    account.setBalance(BigDecimal.ZERO);
                    account.setStatus(AccountStatus.OPEN);
                    accountService.createAccount(account);
                    Notification.show("Счет успешно создан");
                } else {
                    accountService.updateAccount(account);
                    Notification.show("Счет успешно обновлен");
                }
                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (Exception e) {
                Notification.show("Ошибка: " + e.getMessage());
            }
        } else {
            Notification.show("Пожалуйста, заполните все поля корректно");
        }
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        if (parameter != null) {
            try {
                account = accountService.findAccountById(parameter);
                binder.readBean(account);

                idField.setValue(account.getId().toString());
                balanceField.setValue(account.getBalance().toString());
                statusField.setValue(account.getStatus().name());
                createdAtField.setValue(account.getCreatedAt().toString());
                updatedAtField.setValue(account.getUpdatedAt().toString());

                if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                    currency.setReadOnly(true);
                }

                Client currentClient = clientService.findClientById(account.getClientId());
                client.setValue(currentClient);
            } catch (Exception e) {
                Notification.show("Ошибка при загрузке счета: " + e.getMessage());
            }
        } else {
            account = new Account();
        }
    }
}