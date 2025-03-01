package service;

import com.bankapp.model.Client;
import com.bankapp.repository.ClientRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

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
        doNothing().when(clientRepository).save(client);

        // Act
        clientService.createClient(client);

        // Assert
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void hasClients_True() throws SQLException {
        // Arrange
        when(clientRepository.hasClients()).thenReturn(true);

        // Act
        boolean result = clientService.hasClients();

        // Assert
        assertTrue(result);
        verify(clientRepository, times(1)).hasClients();
    }

    @Test
    void hasClients_False() throws SQLException {
        // Arrange
        when(clientRepository.hasClients()).thenReturn(false);

        // Act
        boolean result = clientService.hasClients();

        // Assert
        assertFalse(result);
        verify(clientRepository, times(1)).hasClients();
    }

    @Test
    void findClients_Success() throws SQLException {
        // Arrange
        when(clientRepository.findClients(0, 10, Collections.emptyList())).thenReturn(List.of(client));

        // Act
        List<Client> result = clientService.findClients(0, 10, Collections.emptyList());

        // Assert
        assertEquals(1, result.size());
        assertEquals(client, result.get(0));
        verify(clientRepository, times(1)).findClients(0, 10, Collections.emptyList());
    }

    @Test
    void countClients_Success() throws SQLException {
        // Arrange
        when(clientRepository.countClients()).thenReturn(5);

        // Act
        int result = clientService.countClients();

        // Assert
        assertEquals(5, result);
        verify(clientRepository, times(1)).countClients();
    }

    @Test
    void findClientById_Success() throws SQLException {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(client);

        // Act
        Client result = clientService.findClientById(1L);

        // Assert
        assertEquals(client, result);
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    void findAllClients_Success() throws SQLException {
        // Arrange
        when(clientRepository.findAll()).thenReturn(List.of(client));

        // Act
        List<Client> result = clientService.findAllClients();

        // Assert
        assertEquals(1, result.size());
        assertEquals(client, result.get(0));
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void updateClient_Success() throws SQLException {
        // Arrange
        doNothing().when(clientRepository).update(client);

        // Act
        clientService.updateClient(client);

        // Assert
        verify(clientRepository, times(1)).update(client);
    }
}