package com.bankapp.binderConfigurer;

import com.bankapp.model.Client;
import com.bankapp.ui.client.ClientForm;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class ClientFormBinderConfigurer {

    public static void configureBinder(Binder<Client> binder, ClientForm form) {
        binder.forField(form.getFullName())
                .asRequired("ФИО обязательно")
                .withValidator(new StringLengthValidator("ФИО должно содержать минимум 3 символа", 3, null))
                .bind(Client::getFullName, Client::setFullName);

        binder.forField(form.getPhoneNumber())
                .asRequired("Номер телефона обязателен")
                .withValidator(new RegexpValidator("Формат: +7XXXXXXXXXX", "^\\+7\\d{10}$"))
                .bind(Client::getPhoneNumber, Client::setPhoneNumber);

        binder.forField(form.getInn())
                .asRequired("ИНН обязателен")
                .withValidator(new RegexpValidator("ИНН должен состоять из 12 цифр", "^\\d{12}$"))
                .bind(Client::getInn, Client::setInn);

        binder.forField(form.getAddress())
                .asRequired("Адрес обязателен")
                .bind(Client::getAddress, Client::setAddress);
    }
}
