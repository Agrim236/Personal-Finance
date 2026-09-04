package com.example.skye.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.skye.dto.MonthlyReportDto;
import com.example.skye.dto.YearlyReportDto;
import com.example.skye.entity.User;
import com.example.skye.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("Monthly report: income by category, expenses by category, net savings")
    void monthlyReport() {
        User user = new User("a@example.com", "h", "A", "+1111111111");
        when(transactionRepository.getIncomeByCategory(eq(user), any(), any()))
                .thenReturn(List.of(new Object[]{"Salary", new BigDecimal("50000.00")}));
        when(transactionRepository.getExpensesByCategory(eq(user), any(), any()))
                .thenReturn(List.of(new Object[]{"Food", new BigDecimal("8000.00")},
                        new Object[]{"Rent", new BigDecimal("12000.00")}));

        MonthlyReportDto report = reportService.generateMonthlyReport(user, 1, 2024);

        assertEquals(1, report.getMonth());
        assertEquals(2024, report.getYear());
        assertEquals(new BigDecimal("50000.00"), report.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("8000.00"), report.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("30000.00"), report.getNetSavings());
    }

    @Test
    @DisplayName("Yearly report aggregates income and expenses for the year")
    void yearlyReport() {
        User user = new User("a@example.com", "h", "A", "+1111111111");
        when(transactionRepository.getIncomeByCategory(eq(user), any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{"Salary", new BigDecimal("50000.00")}));
        when(transactionRepository.getExpensesByCategory(eq(user), any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{"Rent", new BigDecimal("24000.00")}));git add .

        YearlyReportDto report = reportService.generateYearlyReport(user, 2024);

        assertEquals(2024, report.getYear());
        assertEquals(new BigDecimal("76000.00"), report.getNetSavings());
    }
}
