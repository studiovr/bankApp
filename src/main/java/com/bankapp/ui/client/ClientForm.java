package com.bankapp.ui.client;

import com.bankapp.exception.DataAccessException;
import com.bankapp.exception.PhoneNumberExistException;
import com.bankapp.model.Client;
import com.bankapp.utils.MessageProvider;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route("client-form")
public class ClientForm extends AbstractClientForm {

    public ClientForm() {
        super();
    }

    @Override
    protected String getFormTitle() {
        return MessageProvider.getMessage("form.title.createClient");
    }

    @Override
    protected void saveClient() {
        Client client = new Client();
        if (binder.writeBeanIfValid(client)) {
            logger.info("Форма валидна. Начинаем сохранение клиента...");
            if (uploadedFile != null) {
                client.setPassportScanCopy(uploadedFile);
            }
            try {
                clientService.createClient(client);
                Notification.show(MessageProvider.getMessage("notification.clientSaved"));
                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (DataAccessException | PhoneNumberExistException e) {
                Notification.show(e.getMessage());
            }
        } else {
            Notification.show(MessageProvider.getMessage("notification.fillFieldsCorrectly"));
        }
    }
}