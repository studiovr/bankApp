package service;

import com.bankapp.enums.Currency;
import com.bankapp.enums.TransactionType;
import com.bankapp.model.Transaction;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setFromAccountId(1L);
        transaction.setToAccountId(2L);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency(Currency.USD);
        transaction.setStatus(TransactionType.TRANSFER);
    }

    @Test
    void createTransaction_Success() throws SQLException {
        // Arrange
        doNothing().when(transactionRepository).save(transaction);

        // Act
        transactionService.createTransaction(1L, 2L, TransactionType.TRANSFER, new BigDecimal("100.00"), Currency.USD);

        // Assert
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void findAllTransactions_Success() throws SQLException {
        // Arrange
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        // Act
        List<Transaction> result = transactionService.findAllTransactions();

        // Assert
        assertEquals(1, result.size());
        assertEquals(transaction, result.get(0));
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void findAllTransactions_Empty() throws SQLException {
        // Arrange
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Transaction> result = transactionService.findAllTransactions();

        // Assert
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findAll();
    }
}