package com.bankapp.service;

import com.bankapp.exception.ClientNotFoundException;
import com.bankapp.exception.DataAccessException;
import com.bankapp.exception.PhoneNumberExistException;
import com.bankapp.repository.ClientRepositoryImpl;
import com.bankapp.ui.client.AbstractClientForm;
import com.bankapp.ui.client.ClientForm;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.bankapp.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class ClientService {

    private final ClientRepositoryImpl clientRepositoryImpl;
    protected static final Logger logger = LoggerFactory.getLogger(AbstractClientForm.class);

    public ClientService(ClientRepositoryImpl clientRepositoryImpl) {
        this.clientRepositoryImpl = clientRepositoryImpl;
    }

    public void createClient(Client client) {
        try {
            if (clientRepositoryImpl.isPhoneNumberExists(client.getPhoneNumber(), null)) {
                throw new PhoneNumberExistException("Номер телефона уже существует");
            }

            clientRepositoryImpl.save(client);
            logger.info("Клиент успешно сохранен: {}", client);
        } catch (SQLException e) {
            logger.error("Ошибка при сохранении клиента", e);
            throw new DataAccessException("Ошибка при создании клиента", e);
        }
    }

    public boolean hasClients() throws SQLException {
        try {
            return clientRepositoryImpl.hasClients();
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при проверке существуют ли клиенты", e);
        }
    }

    public List<Client> findClients(int offset, int limit, List<QuerySortOrder> sortOrders) {
        try {
            return clientRepositoryImpl.findClients(offset, limit, sortOrders);
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке клиентов", e);
            throw new DataAccessException("Ошибка при получении списка клиентов по фильтру", e);
        }
    }

    public int countClients() {
        try {
            return clientRepositoryImpl.count();
        } catch (SQLException e) {
            logger.error("Ошибка при подсчёте клиентов", e);
            throw new DataAccessException("Ошибка при получении количества клиентов", e);
        }
    }

    public Client findClientById(Long id) {
        try {
            return clientRepositoryImpl.findById(id)
                    .orElseThrow(() -> new ClientNotFoundException("Клиент с id=" + id + " не найден"));
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке клиента: {}", e.getMessage());
            throw new DataAccessException("Ошибка при поиске клиента", e);
        }
    }


    public List<Client> findAllClients() {
        try {
            return clientRepositoryImpl.findAll();
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке списка клиентов: {}", e.getMessage(), e);
            throw new DataAccessException("Ошибка при получении списка клиентов", e);
        }
    }

    public void updateClient(Client client) {
        try {
            if (clientRepositoryImpl.isPhoneNumberExists(client.getPhoneNumber(), client.getId())) {
                throw new PhoneNumberExistException("Номер телефона уже существует");
            }

            clientRepositoryImpl.update(client);
            logger.info("Клиент успешно обновлен: {}", client.getId());
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении клиента: {}", e.getMessage());
            throw new DataAccessException("Ошибка при обновлении клиента", e);
        }
    }
}
