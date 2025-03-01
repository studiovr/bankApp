package com.bankapp.ui.client;

import com.bankapp.exception.DataAccessException;
import com.bankapp.exception.PhoneNumberExistException;
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

@Route("edit-client")
public class EditClientForm extends AbstractClientForm implements HasUrlParameter<Long> {

    private TextField idField;
    private TextField createdAtField;
    private TextField updatedAtField;

    private Client client;
    private boolean isEditing = false;
    private String buttonText = MessageProvider.getMessage("button.toggleEdit.disable");

    private Button editButton;

    public EditClientForm() {
        super();
    }

    @Override
    protected void initForm() {
        super.initForm();

        idField = new TextField(MessageProvider.getMessage("field.id"));
        createdAtField = new TextField(MessageProvider.getMessage("field.createdAt"));
        updatedAtField = new TextField(MessageProvider.getMessage("field.updatedAt"));

        idField.setReadOnly(true);
        createdAtField.setReadOnly(true);
        updatedAtField.setReadOnly(true);

        FormLayout formLayout = (FormLayout) getChildren().filter(component -> component instanceof FormLayout).findFirst().orElse(null);
        if (formLayout != null) {
            formLayout.add(idField, createdAtField, updatedAtField);
        }

        editButton = new Button(buttonText, event -> toggleEditingMode());
        VerticalLayout buttonsLayout = (VerticalLayout) getChildren().filter(component -> component instanceof VerticalLayout).findFirst().orElse(null);
        if (buttonsLayout != null) {
            buttonsLayout.add(editButton);
        }

        toggleEditingMode();
    }

    @Override
    protected String getFormTitle() {
        return MessageProvider.getMessage("form.title.editClient");
    }

    @Override
    protected void saveClient() {
        if (binder.writeBeanIfValid(client)) {
            if (uploadedFile != null) {
                client.setPassportScanCopy(uploadedFile);
            }
            try {
                clientService.updateClient(client);
                Notification.show(MessageProvider.getMessage("notification.clientUpdated"));
                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (DataAccessException | PhoneNumberExistException e) {
                Notification.show(e.getMessage());
            }
        } else {
            Notification.show(MessageProvider.getMessage("notification.fillFieldsCorrectly"));
        }
    }

    private void toggleEditingMode() {
        isEditing = !isEditing;
        fullName.setReadOnly(!isEditing);
        phoneNumber.setReadOnly(!isEditing);
        inn.setReadOnly(!isEditing);
        address.setReadOnly(!isEditing);
        upload.setEnabled(isEditing);

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
                client = clientService.findClientById(parameter);
                binder.readBean(client);

                idField.setValue(client.getId().toString());
                createdAtField.setValue(client.getCreatedAt().toString());
                updatedAtField.setValue(client.getUpdatedAt().toString());

                if (client.getPassportScanCopy() != null) {
                    uploadedFile = client.getPassportScanCopy();
                    showUploadedImage(uploadedFile);
                }
            } catch (DataAccessException e) {
                Notification.show(MessageProvider.getMessage("error.loadClients"));
            }
        }
    }
}
