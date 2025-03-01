package service;

import com.bankapp.model.Client;
import com.bankapp.repository.ClientRepositoryImpl;
import com.bankapp.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepositoryImpl clientRepositoryImpl;

    @InjectMocks
    private ClientService clientService;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setFullName("Иван Иванов");
        client.setPhoneNumber("+79991234567");
    }

    @Test
    void createClient_Success() throws SQLException {
        // Arrange
        doNothing().when(clientRepositoryImpl).save(client);

        // Act
        clientService.createClient(client);

        // Assert
        verify(clientRepositoryImpl, times(1)).save(client);
    }

    @Test
    void hasClients_True() throws SQLException {
        // Arrange
        when(clientRepositoryImpl.hasClients()).thenReturn(true);

        // Act
        boolean result = clientService.hasClients();

        // Assert
        assertTrue(result);
        verify(clientRepositoryImpl, times(1)).hasClients();
    }

    @Test
    void hasClients_False() throws SQLException {
        // Arrange
        when(clientRepositoryImpl.hasClients()).thenReturn(false);

        // Act
        boolean result = clientService.hasClients();

        // Assert
        assertFalse(result);
        verify(clientRepositoryImpl, times(1)).hasClients();
    }

    @Test
    void findClients_Success() throws SQLException {
        // Arrange
        when(clientRepositoryImpl.findClients(0, 10, Collections.emptyList())).thenReturn(List.of(client));

        // Act
        List<Client> result = clientService.findClients(0, 10, Collections.emptyList());

        // Assert
        assertEquals(1, result.size());
        assertEquals(client, result.get(0));
        verify(clientRepositoryImpl, times(1)).findClients(0, 10, Collections.emptyList());
    }

    @Test
    void countClients_Success() throws SQLException {
        // Arrange
        when(clientRepositoryImpl.count()).thenReturn(5);

        // Act
        int result = clientService.countClients();

        // Assert
        assertEquals(5, result);
        verify(clientRepositoryImpl, times(1)).count();
    }

    @Test
    void findClientById_Success() throws SQLException {
        // Arrange
        when(clientRepositoryImpl.findById(1L)).thenReturn(Optional.of(client));

        // Act
        Client result = clientService.findClientById(1L);

        // Assert
        assertEquals(client, result);
        verify(clientRepositoryImpl, times(1)).findById(1L);
    }

    @Test
    void findAllClients_Success() throws SQLException {
        // Arrange
        when(clientRepositoryImpl.findAll()).thenReturn(List.of(client));

        // Act
        List<Client> result = clientService.findAllClients();

        // Assert
        assertEquals(1, result.size());
        assertEquals(client, result.get(0));
        verify(clientRepositoryImpl, times(1)).findAll();
    }

    @Test
    void updateClient_Success() throws SQLException {
        // Arrange
        doNothing().when(clientRepositoryImpl).update(client);

        // Act
        clientService.updateClient(client);

        // Assert
        verify(clientRepositoryImpl, times(1)).update(client);
    }
}