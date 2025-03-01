package com.bankapp.ui.account;

import com.bankapp.enums.Currency;
import com.bankapp.exception.DataAccessException;
import com.bankapp.model.Account;
import com.bankapp.model.Client;
import com.bankapp.service.AccountService;
import com.bankapp.service.ClientService;
import com.bankapp.ui.components.StyledParagraph;
import com.bankapp.utils.MessageProvider;
import com.bankapp.utils.ServiceLocator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public abstract class AbstractAccountForm extends VerticalLayout {

    protected final AccountService accountService;
    protected final ClientService clientService;
    protected final Binder<Account> binder = new Binder<>(Account.class);
    protected static final Logger logger = LoggerFactory.getLogger(AbstractAccountForm.class);

    protected final TextField accountNumber = new TextField(MessageProvider.getMessage("account.number"));
    protected final ComboBox<Currency> currency = new ComboBox<>(MessageProvider.getMessage("account.currency"));
    protected final TextField bik = new TextField(MessageProvider.getMessage("account.bik"));
    protected final ComboBox<Client> client = new ComboBox<>(MessageProvider.getMessage("account.client"));

    protected static final String BUTTON_WIDTH = "200px";

    public AbstractAccountForm() {
        this.accountService = ServiceLocator.get(AccountService.class);
        this.clientService = ServiceLocator.get(ClientService.class);
        setAlignItems(Alignment.CENTER);
        initForm();
    }

    protected void initForm() {
        addClassName("account-form");

        StyledParagraph title = new StyledParagraph(getFormTitle());
        add(title);

        initFields();
        initValidation();

        FormLayout formLayout = new FormLayout();
        formLayout.add(accountNumber, currency, bik, client);

        Button saveButton = new Button(MessageProvider.getMessage("button.save"), event -> saveAccount());
        Button cancelButton = new Button(MessageProvider.getMessage("button.cancel"),
                event -> getUI().ifPresent(ui -> ui.navigate("")));

        VerticalLayout buttonsLayout = new VerticalLayout();
        buttonsLayout.setWidthFull();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setPadding(true);
        buttonsLayout.setAlignItems(Alignment.CENTER);

        saveButton.setWidth(BUTTON_WIDTH);
        cancelButton.setWidth(BUTTON_WIDTH);

        buttonsLayout.add(saveButton, cancelButton);
        add(formLayout, buttonsLayout);

        binder.bindInstanceFields(this);
    }

    protected void initFields() {
        try {
            client.setItems(clientService.findAllClients());
            client.setItemLabelGenerator(Client::getFullName);
        } catch (DataAccessException e) {
            Notification.show(e.getMessage());
        }
        currency.setItems(Currency.values());
        currency.setItemLabelGenerator(Currency::getDescription);
    }

    protected abstract String getFormTitle();

    protected abstract void saveAccount();

    protected void initValidation() {
        AccountFormBinderConfigurer.configureBinder(binder, this);
    }
}