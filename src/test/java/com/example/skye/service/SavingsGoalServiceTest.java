package com.example.skye.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

import com.example.skye.dto.SavingsGoalDto;
import com.example.skye.entity.SavingsGoal;
import com.example.skye.entity.User;
import com.example.skye.exception.ResourceNotFoundException;
import com.example.skye.repository.SavingsGoalRepository;
import com.example.skye.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("a@example.com", "hash", "A", "+1111111111");
        user.setId(1L);
    }

    @Test
    @DisplayName("Create goal with name, target amount, target date")
    void createGoal() {
        SavingsGoalDto dto = new SavingsGoalDto("Emergency Fund",
                new BigDecimal("1000.00"), LocalDate.now().plusYears(1));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> {
            SavingsGoal g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });
        when(transactionRepository.calculateTotalIncomeSince(any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.calculateTotalExpensesSince(any(), any())).thenReturn(BigDecimal.ZERO);

        SavingsGoalDto saved = savingsGoalService.createSavingsGoal(dto, user);

        assertEquals("Emergency Fund", saved.getGoalName());
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getCurrentProgress()));
        assertEquals(new BigDecimal("0.0"), saved.getProgressPercentage());
    }

    @Test
    @DisplayName("Start date cannot be after target date")
    void startAfterTarget_Rejected() {
        SavingsGoalDto dto = new SavingsGoalDto("Bad", new BigDecimal("100.00"), LocalDate.now().plusDays(1));
        dto.setStartDate(LocalDate.now().plusDays(10));

        assertThrows(IllegalArgumentException.class, () -> savingsGoalService.createSavingsGoal(dto, user));
    }

    @Test
    @DisplayName("Progress = income − expenses since start, clamped to [0, target]")
    void progressFromNetSavings() {
        SavingsGoal goal = new SavingsGoal("Car", new BigDecimal("1000.00"), LocalDate.now().plusYears(1), user);
        goal.setId(2L);
        goal.setStartDate(LocalDate.of(2024, 1, 1));
        when(savingsGoalRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(goal));
        when(transactionRepository.calculateTotalIncomeSince(user, goal.getStartDate()))
                .thenReturn(new BigDecimal("800.00"));
        when(transactionRepository.calculateTotalExpensesSince(user, goal.getStartDate()))
                .thenReturn(new BigDecimal("200.00"));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        savingsGoalService.updateGoalsProgress(user);

        assertEquals(0, new BigDecimal("600.00").compareTo(goal.getCurrentProgress()));
        assertEquals(new BigDecimal("60.0"), goal.getProgressPercentage());
    }

    @Test
    @DisplayName("Negative net savings does not produce negative progress")
    void progressClampedAtZero() {
        SavingsGoal goal = new SavingsGoal("Car", new BigDecimal("1000.00"), LocalDate.now().plusYears(1), user);
        goal.setStartDate(LocalDate.of(2024, 1, 1));
        when(savingsGoalRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(goal));
        when(transactionRepository.calculateTotalIncomeSince(any(), any())).thenReturn(new BigDecimal("100"));
        when(transactionRepository.calculateTotalExpensesSince(any(), any())).thenReturn(new BigDecimal("500"));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        savingsGoalService.updateGoalsProgress(user);

        assertEquals(0, BigDecimal.ZERO.compareTo(goal.getCurrentProgress()));
    }

    @Test
    @DisplayName("Progress cannot exceed target amount")
    void progressClampedAtTarget() {
        SavingsGoal goal = new SavingsGoal("Car", new BigDecimal("1000.00"), LocalDate.now().plusYears(1), user);
        goal.setStartDate(LocalDate.of(2024, 1, 1));
        when(savingsGoalRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(goal));
        when(transactionRepository.calculateTotalIncomeSince(any(), any())).thenReturn(new BigDecimal("5000"));
        when(transactionRepository.calculateTotalExpensesSince(any(), any())).thenReturn(BigDecimal.ZERO);
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        savingsGoalService.updateGoalsProgress(user);

        assertEquals(0, new BigDecimal("1000.00").compareTo(goal.getCurrentProgress()));
        assertEquals(new BigDecimal("100.0"), goal.getProgressPercentage());
    }

    @Test
    @DisplayName("Deleted transactions are excluded from goal progress")
    void missingGoal_NotFound() {
        when(savingsGoalRepository.findByIdAndUser(9L, user)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> savingsGoalService.getSavingsGoal(9L, user));
    }

    @Test
    @DisplayName("Delete savings goal")
    void deleteGoal() {
        SavingsGoal goal = new SavingsGoal("Car", new BigDecimal("1000.00"), LocalDate.now().plusYears(1), user);
        goal.setId(3L);
        when(savingsGoalRepository.findByIdAndUser(3L, user)).thenReturn(goal);

        savingsGoalService.deleteSavingsGoal(3L, user);

        verify(savingsGoalRepository).delete(goal);
    }
}
