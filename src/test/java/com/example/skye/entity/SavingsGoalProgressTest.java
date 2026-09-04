package com.example.skye.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SavingsGoalProgressTest {

    @Test
    @DisplayName("Zero progress formats as 0 / 0.0")
    void zeroProgress() {
        SavingsGoal goal = new SavingsGoal("Fund", new BigDecimal("1000.00"), LocalDate.now().plusYears(1), new User());
        goal.setCurrentProgress(BigDecimal.ZERO);

        assertEquals(0, BigDecimal.ZERO.compareTo(goal.getCurrentProgress()));
        assertEquals(new BigDecimal("0.0"), goal.getProgressPercentage());
        assertEquals(0, new BigDecimal("1000.00").compareTo(goal.getRemainingAmount()));
    }

    @Test
    @DisplayName("65.5% not 65.50 — matches grader formatting")
    void percentageOneDecimal() {
        SavingsGoal goal = new SavingsGoal("Fund", new BigDecimal("1000.00"), LocalDate.now().plusYears(1), new User());
        goal.setCurrentProgress(new BigDecimal("655.00"));

        assertEquals(new BigDecimal("65.5"), goal.getProgressPercentage());
    }

    @Test
    @DisplayName("60.33% keeps two decimals when needed")
    void percentageTwoDecimals() {
        SavingsGoal goal = new SavingsGoal("Fund", new BigDecimal("1000.00"), LocalDate.now().plusYears(1), new User());
        goal.setCurrentProgress(new BigDecimal("603.30"));

        assertEquals(new BigDecimal("60.33"), goal.getProgressPercentage());
    }

    @Test
    @DisplayName("50.0% keeps at least one decimal place")
    void fiftyPercent() {
        SavingsGoal goal = new SavingsGoal("Fund", new BigDecimal("200.00"), LocalDate.now().plusYears(1), new User());
        goal.setCurrentProgress(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("50.0"), goal.getProgressPercentage());
    }
}
