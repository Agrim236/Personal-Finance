package com.example.skye.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skye.dto.MonthlyReportDto;
import com.example.skye.dto.YearlyReportDto;
import com.example.skye.entity.User;
import com.example.skye.repository.TransactionRepository;


@Service
@Transactional(readOnly = true)
public class ReportService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
 
    public MonthlyReportDto generateMonthlyReport(User user, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        Map<String, BigDecimal> incomeByCategory = getCategoryTotals(
            transactionRepository.getIncomeByCategory(user, startDate, endDate));
        
        Map<String, BigDecimal> expensesByCategory = getCategoryTotals(
            transactionRepository.getExpensesByCategory(user, startDate, endDate));
        
        BigDecimal totalIncome = incomeByCategory.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expensesByCategory.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);
        
        return new MonthlyReportDto(month, year, incomeByCategory, expensesByCategory, netSavings);
    }
    

    public YearlyReportDto generateYearlyReport(User user, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        
        Map<String, BigDecimal> incomeByCategory = getCategoryTotals(
            transactionRepository.getIncomeByCategory(user, startDate, endDate));
        
        Map<String, BigDecimal> expensesByCategory = getCategoryTotals(
            transactionRepository.getExpensesByCategory(user, startDate, endDate));
        
        BigDecimal totalIncome = incomeByCategory.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expensesByCategory.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);
        
        return new YearlyReportDto(year, incomeByCategory, expensesByCategory, netSavings);
    }
    

    private Map<String, BigDecimal> getCategoryTotals(List<Object[]> queryResults) {
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        for (Object[] result : queryResults) {
            String categoryName = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            categoryTotals.put(categoryName, amount);
        }
        return categoryTotals;
    }
}
