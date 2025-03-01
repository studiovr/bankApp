package com.bankapp.ui.account;

import com.bankapp.utils.DIContainer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bankapp.model.Account;
import com.bankapp.service.AccountService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

@Route("account-list")
public class AccountListView extends VerticalLayout {

    private final AccountService accountService;
    private final Grid<Account> accountGrid = new Grid<>(Account.class);
    private static final Logger logger = LoggerFactory.getLogger(AccountListView.class);

    public AccountListView() {
        this.accountService = DIContainer.get(AccountService.class);
        setSizeFull();
        initAccountGrid();

        Button backButton = new Button("Назад", event -> getUI().ifPresent(ui -> ui.navigate("")));
        add(backButton, accountGrid);
    }

    private void initAccountGrid() {
        configureGridColumns();
        configureDataProvider();
        accountGrid.setHeightFull();
        accountGrid.setPageSize(10);
    }

    private void configureGridColumns() {
        accountGrid.setColumns("accountNumber", "balance", "status", "bik", "currency");
        accountGrid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        accountGrid.addComponentColumn(account ->
                new Button("Редактировать", new Icon(VaadinIcon.EDIT), event ->
                        getUI().ifPresent(ui -> ui.navigate("edit-account/" + account.getId()))));
    }

    private void configureDataProvider() {
        DataProvider<Account, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    try {
                        int offset = query.getOffset();
                        int limit = query.getLimit();

                        List<QuerySortOrder> sortOrders = query.getSortOrders();
                        return accountService.findAccounts(offset, limit, sortOrders).stream();
                    } catch (SQLException e) {
                        logger.error("Ошибка при загрузке счетов", e);
                        Notification.show("Ошибка при загрузке счетов: " + e.getMessage());
                        return Stream.empty();
                    }
                },
                query -> {
                    try {
                        return accountService.countAccounts();
                    } catch (SQLException e) {
                        logger.error("Ошибка при подсчете счетов", e);
                        Notification.show("Ошибка при подсчете счетов: " + e.getMessage());
                        return 0;
                    }
                }
        );

        accountGrid.setDataProvider(dataProvider);
    }
}
