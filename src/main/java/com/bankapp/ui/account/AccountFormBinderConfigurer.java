package com.bankapp.ui.account;

import com.bankapp.model.Account;
import com.bankapp.utils.MessageProvider;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class AccountFormBinderConfigurer {

    public static void configureBinder(Binder<Account> binder, AbstractAccountForm form) {
        binder.forField(form.accountNumber)
                .asRequired(MessageProvider.getMessage("validation.accountNumber.required"))
                .withValidator(new StringLengthValidator(
                        MessageProvider.getMessage("validation.accountNumber.length"), 3, null))
                .bind(Account::getAccountNumber, Account::setAccountNumber);

        binder.forField(form.currency)
                .asRequired(MessageProvider.getMessage("validation.currency.required"))
                .bind(Account::getCurrency, Account::setCurrency);

        binder.forField(form.bik)
                .asRequired(MessageProvider.getMessage("validation.bik.required"))
                .withValidator(new RegexpValidator(
                        MessageProvider.getMessage("validation.bik.format"), "^\\d{9}$"))
                .bind(Account::getBik, Account::setBik);

        binder.forField(form.client)
                .asRequired(MessageProvider.getMessage("validation.client.required"))
                .bind(account -> null, (account, selectedClient) -> account.setClientId(selectedClient.getId()));
    }
}