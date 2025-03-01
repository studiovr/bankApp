package com.bankapp.ui.client;

import com.bankapp.model.Client;
import com.bankapp.utils.MessageProvider;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class ClientFormBinderConfigurer {

    public static void configureBinder(Binder<Client> binder, AbstractClientForm form) {
        binder.forField(form.fullName)
                .asRequired(MessageProvider.getMessage("validation.fullName.required"))
                .withValidator(new StringLengthValidator(
                        MessageProvider.getMessage("validation.fullName.length"), 3, null))
                .bind(Client::getFullName, Client::setFullName);

        binder.forField(form.phoneNumber)
                .asRequired(MessageProvider.getMessage("validation.phoneNumber.required"))
                .withValidator(new RegexpValidator(
                        MessageProvider.getMessage("validation.phoneNumber.format"), "^\\+7\\d{10}$"))
                .bind(Client::getPhoneNumber, Client::setPhoneNumber);

        binder.forField(form.inn)
                .asRequired(MessageProvider.getMessage("validation.inn.required"))
                .withValidator(new RegexpValidator(
                        MessageProvider.getMessage("validation.inn.format"), "^\\d{12}$"))
                .bind(Client::getInn, Client::setInn);

        binder.forField(form.address)
                .asRequired(MessageProvider.getMessage("validation.address.required"))
                .bind(Client::getAddress, Client::setAddress);
    }
}