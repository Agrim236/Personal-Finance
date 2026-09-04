package com.example.skye.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.skye.dto.TransactionDto;
import com.example.skye.entity.Category;
import com.example.skye.entity.Transaction;
import com.example.skye.entity.User;
import com.example.skye.exception.ResourceNotFoundException;
import com.example.skye.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private SavingsGoalService savingsGoalService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private User otherUser;
    private Category salary;
    private Transaction existing;

    @BeforeEach
    void setUp() {
        user = new User("a@example.com", "hash", "A", "+1111111111");
        user.setId(1L);
        otherUser = new User("b@example.com", "hash", "B", "+2222222222");
        otherUser.setId(2L);
        salary = new Category("Salary", Category.CategoryType.INCOME, false, null);
        existing = new Transaction(new BigDecimal("50000.00"), LocalDate.of(2024, 1, 15),
                salary, "January salary", user);
        existing.setId(5L);
    }

    @Test
    @DisplayName("Create transaction with amount, date, category, description")
    void createTransaction() {
        TransactionDto dto = new TransactionDto(new BigDecimal("50000.00"),
                LocalDate.of(2024, 1, 15), "Salary", "January salary");
        when(categoryService.findCategoryByName("Salary", user)).thenReturn(salary);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(5L);
            return t;
        });

        TransactionDto saved = transactionService.createTransaction(dto, user);

        assertEquals(5L, saved.getId());
        assertEquals("Salary", saved.getCategory());
        assertEquals("INCOME", saved.getType());
        verify(savingsGoalService).updateGoalsProgress(user);
    }

    @Test
    @DisplayName("List transactions newest first")
    void getAllTransactions() {
        when(transactionRepository.findByUserOrderByDateDescCreatedAtDesc(user))
                .thenReturn(List.of(existing));

        List<TransactionDto> list = transactionService.getAllTransactions(user);

        assertEquals(1, list.size());
        assertEquals(new BigDecimal("50000.00"), list.get(0).getAmount());
    }

    @Test
    @DisplayName("Filter by date range and category")
    void filterTransactions() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);
        when(categoryService.findCategoryByName("Salary", user)).thenReturn(salary);
        when(transactionRepository.findByUserAndCategoryAndDateBetweenOrderByDateDescCreatedAtDesc(
                user, salary, start, end)).thenReturn(List.of(existing));

        List<TransactionDto> list = transactionService.getTransactionsWithFilters(user, start, end, "Salary");

        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Update ignores date (PDF: date cannot be changed)")
    void updateDoesNotChangeDate() {
        TransactionDto dto = new TransactionDto();
        dto.setAmount(new BigDecimal("60000.00"));
        dto.setDate(LocalDate.of(2020, 1, 1));
        dto.setDescription("Updated");
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionDto updated = transactionService.updateTransaction(5L, dto, user);

        assertEquals(LocalDate.of(2024, 1, 15), updated.getDate());
        assertEquals(new BigDecimal("60000.00"), updated.getAmount());
        assertEquals("Updated", updated.getDescription());
    }

    @Test
    @DisplayName("User cannot update another user's transaction")
    void updateOtherUser_Denied() {
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class,
                () -> transactionService.updateTransaction(5L, new TransactionDto(), otherUser));
    }

    @Test
    @DisplayName("Delete transaction recalculates savings goals")
    void deleteTransaction() {
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(existing));

        transactionService.deleteTransaction(5L, user);

        verify(transactionRepository).delete(existing);
        verify(savingsGoalService).updateGoalsProgress(user);
    }

    @Test
    @DisplayName("Missing transaction id is not found")
    void missingTransaction() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction(99L, user));
    }
}
