package com.example.skye.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.skye.dto.SavingsGoalDto;
import com.example.skye.entity.User;
import com.example.skye.service.SavingsGoalService;
import com.example.skye.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/goals")
public class SavingsGoalController {

    @Autowired
    private SavingsGoalService savingsGoalService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<SavingsGoalDto> createSavingsGoal(@Valid @RequestBody SavingsGoalDto goalDto,
                                                            HttpSession session) {
        try {
            if (goalDto.getGoalName() == null || goalDto.getGoalName().trim().isEmpty()
                    || goalDto.getTargetAmount() == null
                    || goalDto.getTargetDate() == null) {
                return ResponseEntity.badRequest().build();
            }
            User user = getCurrentUser(session);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savingsGoalService.createSavingsGoal(goalDto, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSavingsGoals(HttpSession session) {
        try {
            User user = getCurrentUser(session);
            List<SavingsGoalDto> goals = savingsGoalService.getAllSavingsGoals(user);
            Map<String, Object> body = new HashMap<>();
            body.put("goals", goals);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalDto> getSavingsGoal(@PathVariable Long id, HttpSession session) {
        try {
            User user = getCurrentUser(session);
            return ResponseEntity.ok(savingsGoalService.getSavingsGoal(id, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalDto> updateSavingsGoal(@PathVariable Long id,
                                                            @Valid @RequestBody SavingsGoalDto goalDto,
                                                            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            return ResponseEntity.ok(savingsGoalService.updateSavingsGoal(id, goalDto, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSavingsGoal(@PathVariable Long id, HttpSession session) {
        try {
            User user = getCurrentUser(session);
            savingsGoalService.deleteSavingsGoal(id, user);
            return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
