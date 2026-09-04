package com.example.skye.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skye.dto.TransactionDto;
import com.example.skye.entity.User;
import com.example.skye.service.TransactionService;
import com.example.skye.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody TransactionDto transactionDto,
                                                            HttpSession session) {
        try {
            if (transactionDto.getAmount() == null
                    || transactionDto.getDate() == null
                    || transactionDto.getCategory() == null
                    || transactionDto.getCategory().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            User user = getCurrentUser(session);
            TransactionDto created = transactionService.createTransaction(transactionDto, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            List<TransactionDto> transactions;
            if (startDate != null || endDate != null || category != null) {
                transactions = transactionService.getTransactionsWithFilters(user, startDate, endDate, category);
            } else {
                transactions = transactionService.getAllTransactions(user);
            }
            Map<String, Object> body = new HashMap<>();
            body.put("transactions", transactions);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(@PathVariable Long id,
                                                            @Valid @RequestBody TransactionDto transactionDto,
                                                            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            return ResponseEntity.ok(transactionService.updateTransaction(id, transactionDto, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(@PathVariable Long id, HttpSession session) {
        try {
            User user = getCurrentUser(session);
            transactionService.deleteTransaction(id, user);
            return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return userService.findById(userId);
    }
}
