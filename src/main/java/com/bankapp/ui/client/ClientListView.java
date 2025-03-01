package com.bankapp.ui.client;

import com.bankapp.exception.DataAccessException;
import com.bankapp.utils.MessageProvider;
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
import com.bankapp.model.Client;
import com.bankapp.service.ClientService;

import java.util.List;
import java.util.stream.Stream;

@Route("client-list")
public class ClientListView extends VerticalLayout {

    private final ClientService clientService;
    private final Grid<Client> clientGrid = new Grid<>(Client.class);

    public ClientListView() {
        this.clientService = ServiceLocator.get(ClientService.class);

        setSizeFull();

        initClientGrid();

        Button backButton = new Button(MessageProvider.getMessage("button.back"), event -> getUI().ifPresent(ui -> ui.navigate("")));

        add(backButton, clientGrid);
    }

    private void initClientGrid() {
        clientGrid.setColumns("id", "fullName", "phoneNumber", "inn", "address");
        clientGrid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setSortable(true);
        });

        clientGrid.addComponentColumn(client ->
                new Button(MessageProvider.getMessage("button.edit"), new Icon(VaadinIcon.EDIT), event ->
                        getUI().ifPresent(ui -> ui.navigate("edit-client/" + client.getId()))));

        DataProvider<Client, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    try {
                        int offset = query.getOffset();
                        int limit = query.getLimit();
                        List<QuerySortOrder> sortOrders = query.getSortOrders();
                        return clientService.findClients(offset, limit, sortOrders).stream();
                    } catch (DataAccessException e) {
                        Notification.show(MessageProvider.getMessage("error.loadClients"));
                        return Stream.empty();
                    }
                },
                query -> {
                    try {
                        return clientService.countClients();
                    } catch (DataAccessException e) {
                        Notification.show(MessageProvider.getMessage("error.loadClients"));
                        return 0;
                    }
                }
        );

        clientGrid.setDataProvider(dataProvider);

        clientGrid.setHeightFull();
        clientGrid.setPageSize(5);
    }
}
