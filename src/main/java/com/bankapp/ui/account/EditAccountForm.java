package com.bankapp.ui.account;

import com.bankapp.enums.AccountStatus;
import com.bankapp.exception.AccountExistException;
import com.bankapp.exception.DataAccessException;
import com.bankapp.model.Account;
import com.bankapp.model.Client;
import com.bankapp.utils.MessageProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;

@Route("edit-account")
public class EditAccountForm extends AbstractAccountForm implements HasUrlParameter<Long> {

    private TextField idField;
    private TextField balanceField;
    private TextField statusField;
    private TextField createdAtField;
    private TextField updatedAtField;

    private Account account;
    private boolean isEditing = false;
    private String buttonText = MessageProvider.getMessage("button.toggleEdit.disable");

    private Button editButton;

    public EditAccountForm() {
        super();
    }

    @Override
    protected void initForm() {
        super.initForm();

        idField = new TextField(MessageProvider.getMessage("field.id"));
        idField.setReadOnly(true);

        balanceField = new TextField(MessageProvider.getMessage("field.balance"));
        balanceField.setReadOnly(true);

        statusField = new TextField(MessageProvider.getMessage("field.status"));
        statusField.setReadOnly(true);

        createdAtField = new TextField(MessageProvider.getMessage("field.createdAt"));
        createdAtField.setReadOnly(true);

        updatedAtField = new TextField(MessageProvider.getMessage("field.updatedAt"));
        updatedAtField.setReadOnly(true);

        FormLayout formLayout = (FormLayout) getChildren().filter(component -> component instanceof FormLayout).findFirst().orElse(null);
        if (formLayout != null) {
            formLayout.add(idField, balanceField, statusField, createdAtField, updatedAtField);
        }

        editButton = new Button(buttonText, event -> toggleEditingMode());
        VerticalLayout buttonsLayout = (VerticalLayout) getChildren().filter(component -> component instanceof VerticalLayout).findFirst().orElse(null);
        if (buttonsLayout != null) {
            buttonsLayout.addComponentAsFirst(editButton);
        }

        toggleEditingMode();
    }

    @Override
    protected String getFormTitle() {
        return MessageProvider.getMessage("form.title.editAccount");
    }

    @Override
    protected void saveAccount() {
        if (binder.writeBeanIfValid(account)) {
            try {
                accountService.updateAccount(account);
                Notification.show(MessageProvider.getMessage("notification.accountUpdated"));
                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (DataAccessException | AccountExistException e) {
                Notification.show(e.getMessage());
            }
        } else {
            Notification.show(MessageProvider.getMessage("notification.fillFieldsCorrectly"));
        }
    }

    private void toggleEditingMode() {
        isEditing = !isEditing;
        accountNumber.setReadOnly(!isEditing);
        currency.setReadOnly(!isEditing);
        bik.setReadOnly(!isEditing);
        client.setReadOnly(!isEditing);

        if (isEditing) {
            buttonText = MessageProvider.getMessage("button.toggleEdit.disable");
        } else {
            buttonText = MessageProvider.getMessage("button.toggleEdit.enable");
        }
        editButton.setText(buttonText);
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
            } catch (DataAccessException e) {
                Notification.show(MessageProvider.getMessage("error.loadAccount"));
            }
        } else {
            account = new Account();
        }
    }
}