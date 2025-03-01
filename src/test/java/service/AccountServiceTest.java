package service;

import com.bankapp.enums.AccountStatus;
import com.bankapp.enums.Currency;
import com.bankapp.enums.TransactionType;
import com.bankapp.exception.AccountNotFoundException;
import com.bankapp.exception.InsufficientFundsException;
import com.bankapp.model.Account;
import com.bankapp.repository.AccountRepository;
import com.bankapp.service.AccountService;
import com.bankapp.service.TransactionService;
import com.bankapp.utils.TransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountService accountService;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setBalance(new BigDecimal("1000.00"));
        fromAccount.setStatus(AccountStatus.OPEN);

        toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount.setStatus(AccountStatus.OPEN);
    }

    @Test
    void transferFunds_Success() throws SQLException {
        // Arrange
        fromAccount.setCurrency(Currency.USD);
        toAccount.setCurrency(Currency.USD);
        when(accountRepository.findById(1L)).thenReturn(fromAccount);
        when(accountRepository.findById(2L)).thenReturn(toAccount);

        // Act
        accountService.transferFunds(1L, 2L, new BigDecimal("200.00"), Currency.USD);

        // Assert
        assertEquals(new BigDecimal("800.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("700.00"), toAccount.getBalance());
        verify(accountRepository, times(1)).update(fromAccount);
        verify(accountRepository, times(1)).update(toAccount);
        verify(transactionService, times(1)).createTransaction(1L, 2L, TransactionType.TRANSFER, new BigDecimal("200.00"), Currency.USD);
        verify(transactionManager, times(1)).beginTransaction();
        verify(transactionManager, times(1)).commitTransaction();
        verify(transactionManager, never()).rollbackTransaction();
    }

    @Test
    void transferFunds_AccountNotFound() throws SQLException {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(null);

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                accountService.transferFunds(1L, 2L, new BigDecimal("200.00"), Currency.USD));
        verify(transactionManager, times(1)).beginTransaction();
        verify(transactionManager, times(1)).rollbackTransaction();
        verify(transactionManager, never()).commitTransaction();
    }

    @Test
    void transferFunds_InsufficientFunds() throws SQLException {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(fromAccount);
        when(accountRepository.findById(2L)).thenReturn(toAccount);

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () ->
                accountService.transferFunds(1L, 2L, new BigDecimal("1500.00"), Currency.USD));
        verify(transactionManager, times(1)).beginTransaction();
        verify(transactionManager, times(1)).rollbackTransaction();
        verify(transactionManager, never()).commitTransaction();
    }

    @Test
    void depositFunds_Success() throws SQLException {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(fromAccount);

        // Act
        accountService.depositFunds(1L, new BigDecimal("200.00"));

        // Assert
        assertEquals(new BigDecimal("1200.00"), fromAccount.getBalance());
        verify(accountRepository, times(1)).update(fromAccount);
    }

    @Test
    void depositFunds_AccountNotFound() throws SQLException {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(null);

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                accountService.depositFunds(1L, new BigDecimal("200.00")));
    }

    @Test
    void depositFunds_InvalidAmount() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                accountService.depositFunds(1L, new BigDecimal("-100.00")));
    }

    @Test
    void createAccount_Success() throws SQLException {
        // Arrange
        Account newAccount = new Account();
        newAccount.setAccountNumber("123456789");
        newAccount.setStatus(AccountStatus.OPEN);
        newAccount.setBalance(BigDecimal.ZERO);

        // Act
        accountService.createAccount(newAccount);

        // Assert
        verify(accountRepository, times(1)).save(newAccount);
    }

    @Test
    void closeAccount_Success() throws SQLException {
        // Arrange
        doNothing().when(accountRepository).closeAccount(1L);

        // Act
        accountService.closeAccount(1L);

        // Assert
        verify(accountRepository, times(1)).closeAccount(1L);
    }
}