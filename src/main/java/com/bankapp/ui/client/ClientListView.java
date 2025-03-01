package com.bankapp.ui.client;

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
import com.bankapp.model.Client;
import com.bankapp.service.ClientService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

@Route("client-list")
public class ClientListView extends VerticalLayout {

    private final ClientService clientService;
    private final Grid<Client> clientGrid = new Grid<>(Client.class);
    private static final Logger logger = LoggerFactory.getLogger(ClientListView.class);

    public ClientListView() {
        this.clientService = DIContainer.get(ClientService.class);

        setSizeFull();

        initClientGrid();

        Button backButton = new Button("Назад", event -> getUI().ifPresent(ui -> ui.navigate("")));

        add(backButton, clientGrid);
    }

    private void initClientGrid() {
        clientGrid.setColumns("id", "fullName", "phoneNumber", "inn", "address");
        clientGrid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        clientGrid.addComponentColumn(client ->
                new Button("Редактировать", new Icon(VaadinIcon.EDIT), event ->
                        getUI().ifPresent(ui -> ui.navigate("edit-client/" + client.getId()))));

        DataProvider<Client, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    try {
                        int offset = query.getOffset();
                        int limit = query.getLimit();
                        List<QuerySortOrder> sortOrders = query.getSortOrders();
                        return clientService.findClients(offset, limit, sortOrders).stream();
                    } catch (SQLException e) {
                        logger.error("Ошибка при загрузке клиентов", e);
                        Notification.show("Ошибка при загрузке клиентов: " + e.getMessage());
                        return Stream.empty();
                    }
                },
                query -> {
                    try {
                        return clientService.countClients();
                    } catch (SQLException e) {
                        logger.error("Ошибка при подсчёте клиентов", e);
                        Notification.show("Ошибка при подсчете клиентов: " + e.getMessage());
                        return 0;
                    }
                }
        );

        clientGrid.setDataProvider(dataProvider);

        clientGrid.setHeightFull();
        clientGrid.setPageSize(5);
    }
}
