package com.bankapp.ui.account;

import com.bankapp.exception.AccountExistException;
import com.bankapp.exception.DataAccessException;
import com.bankapp.model.Account;
import com.bankapp.utils.MessageProvider;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route("account-form")
public class AccountForm extends AbstractAccountForm {

    public AccountForm() {
        super();
    }

    @Override
    protected String getFormTitle() {
        return MessageProvider.getMessage("form.title.createAccount");
    }

    @Override
    protected void saveAccount() {
        Account account = new Account();
        if (binder.writeBeanIfValid(account)) {
            try {
                logger.info("Сохранение нового счета для клиента с id={}", account.getClientId());

                accountService.createAccount(account);

                Notification.show(MessageProvider.getMessage("notification.accountSaved"));
                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (DataAccessException | AccountExistException e) {
                Notification.show(e.getMessage());
            }
        } else {
            Notification.show(MessageProvider.getMessage("notification.fillFieldsCorrectly"));
        }
    }
}