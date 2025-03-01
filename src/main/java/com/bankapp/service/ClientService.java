package com.bankapp.service;

import com.bankapp.repository.ClientRepository;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.bankapp.model.Client;

import java.sql.SQLException;
import java.util.List;

public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void createClient(Client client) throws SQLException {
        if (clientRepository.isPhoneNumberExists(client.getPhoneNumber(), null)) {
            throw new SQLException("Номер телефона уже существует");
        }

        clientRepository.save(client);
    }

    public boolean hasClients() throws SQLException {
        return clientRepository.hasClients();
    }

    public List<Client> findClients(int offset, int limit, List<QuerySortOrder> sortOrders) throws SQLException {
        return clientRepository.findClients(offset, limit, sortOrders);
    }

    public int countClients() throws SQLException {
        return clientRepository.countClients();
    }

    public Client findClientById(Long id) throws SQLException {
        return clientRepository.findById(id);
    }

    public List<Client> findAllClients() throws SQLException {
        return clientRepository.findAll();
    }

    public void updateClient(Client client) throws SQLException {
        clientRepository.update(client);
    }
}
