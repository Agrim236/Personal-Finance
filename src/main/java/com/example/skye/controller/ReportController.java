package com.example.skye.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.skye.dto.MonthlyReportDto;
import com.example.skye.dto.YearlyReportDto;
import com.example.skye.entity.User;
import com.example.skye.service.ReportService;
import com.example.skye.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportDto> getMonthlyReport(@PathVariable int year,
                                                             @PathVariable int month,
                                                             HttpSession session) {
        try {
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest().build();
            }
            User user = getCurrentUser(session);
            return ResponseEntity.ok(reportService.generateMonthlyReport(user, month, year));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportDto> getYearlyReport(@PathVariable int year, HttpSession session) {
        try {
            User user = getCurrentUser(session);
            return ResponseEntity.ok(reportService.generateYearlyReport(user, year));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
