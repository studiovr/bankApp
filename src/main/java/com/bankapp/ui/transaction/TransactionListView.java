package com.bankapp.ui.transaction;

import com.bankapp.exception.TransactionException;
import com.bankapp.model.Transaction;
import com.bankapp.service.TransactionService;
import com.bankapp.utils.ServiceLocator;
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

import java.util.List;
import java.util.stream.Stream;

@Route("transaction-list")
public class TransactionListView extends VerticalLayout {

    private final TransactionService transactionService;
    private final Grid<Transaction> transactionGrid = new Grid<>(Transaction.class);

    public TransactionListView() {
        this.transactionService = ServiceLocator.get(TransactionService.class);
        setSizeFull();
        initTransactionGrid();

        Button backButton = new Button("Назад", new Icon(VaadinIcon.ARROW_LEFT), event ->
                getUI().ifPresent(ui -> ui.navigate("")));
        add(backButton, transactionGrid);
    }

    private void initTransactionGrid() {
        configureGridColumns();
        configureDataProvider();
        transactionGrid.setHeightFull();
        transactionGrid.setPageSize(10);
    }

    private void configureGridColumns() {
        // Настраиваем колонки для отображения данных о транзакциях
        transactionGrid.setColumns("fromAccount", "toAccount", "amount", "currency", "transactionDate", "type");

        // Настраиваем автоматическую ширину и возможность сортировки для всех колонок
        transactionGrid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });
    }

    private void configureDataProvider() {
        DataProvider<Transaction, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    try {
                        int offset = query.getOffset();
                        int limit = query.getLimit();

                        List<QuerySortOrder> sortOrders = query.getSortOrders();
                        return transactionService.findTransactions(offset, limit, sortOrders).stream();
                    } catch (TransactionException e) {
                        Notification.show(e.getMessage());
                        return Stream.empty();
                    }
                },
                query -> {
                    try {
                        return transactionService.countTransactions();
                    } catch (TransactionException e) {
                        Notification.show(e.getMessage());
                        return 0;
                    }
                }
        );

        transactionGrid.setDataProvider(dataProvider);
    }
}