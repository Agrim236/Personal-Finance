package com.example.skye.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skye.dto.TransactionDto;
import com.example.skye.entity.Category;
import com.example.skye.entity.Transaction;
import com.example.skye.entity.User;
import com.example.skye.exception.ResourceNotFoundException;
import com.example.skye.repository.TransactionRepository;


@Service
@Transactional
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private SavingsGoalService savingsGoalService;
    

    public TransactionDto createTransaction(TransactionDto transactionDto, User user) {
        Category category = categoryService.findCategoryByName(transactionDto.getCategory(), user);
        
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDto.getAmount());
        transaction.setDate(transactionDto.getDate());
        transaction.setCategory(category);
        transaction.setDescription(transactionDto.getDescription());
        transaction.setUser(user);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        savingsGoalService.updateGoalsProgress(user);
        
        return convertToDto(savedTransaction);
    }
    

    public List<TransactionDto> getAllTransactions(User user) {
        List<Transaction> transactions = transactionRepository.findByUserOrderByDateDescCreatedAtDesc(user);
        return transactions.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<TransactionDto> getTransactionsWithFilters(User user, LocalDate startDate, 
                                                          LocalDate endDate, String categoryName) {
        List<Transaction> transactions;
        
        if (categoryName != null && !categoryName.isEmpty()) {
            Category category = categoryService.findCategoryByName(categoryName, user);
            if (startDate != null && endDate != null) {
                transactions = transactionRepository.findByUserAndCategoryAndDateBetweenOrderByDateDescCreatedAtDesc(
                    user, category, startDate, endDate);
            } else {
                transactions = transactionRepository.findByUserAndCategoryOrderByDateDescCreatedAtDesc(user, category);
            }
        } else if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserAndDateBetweenOrderByDateDescCreatedAtDesc(
                user, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserOrderByDateDescCreatedAtDesc(user);
        }
        
        return transactions.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
 
    public TransactionDto updateTransaction(Long id, TransactionDto transactionDto, User user) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        if (transactionDto.getAmount() != null) {
            transaction.setAmount(transactionDto.getAmount());
        }
        if (transactionDto.getCategory() != null) {
            Category category = categoryService.findCategoryByName(transactionDto.getCategory(), user);
            transaction.setCategory(category);
        }
        if (transactionDto.getDescription() != null) {
            transaction.setDescription(transactionDto.getDescription());
        }
        // Note: Date field is intentionally ignored as per requirements
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        savingsGoalService.updateGoalsProgress(user);
        
        return convertToDto(savedTransaction);
    }
    

    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        transactionRepository.delete(transaction);
        
        savingsGoalService.updateGoalsProgress(user);
    }
    
   
    private TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setDate(transaction.getDate());
        dto.setCategory(transaction.getCategory().getName());
        dto.setDescription(transaction.getDescription());
        dto.setType(transaction.getCategory().getType().toString());
        return dto;
    }
}
