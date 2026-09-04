package com.example.skye.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;


public class SavingsGoalDto {
    
    private Long id;
    
    private String goalName;
    
    @DecimalMin(value = "0.01", message = "Target amount must be positive")
    private BigDecimal targetAmount;
    
    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;
    
    private LocalDate startDate;
    
    private BigDecimal currentProgress;
    
    private BigDecimal progressPercentage;
    
    private BigDecimal remainingAmount;
    
    public SavingsGoalDto() {}
    
    public SavingsGoalDto(String goalName, BigDecimal targetAmount, LocalDate targetDate) {
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.startDate = LocalDate.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getGoalName() {
        return goalName;
    }
    
    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }
    
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }
    
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
    
    public LocalDate getTargetDate() {
        return targetDate;
    }
    
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public BigDecimal getCurrentProgress() {
        return currentProgress;
    }
    
    public void setCurrentProgress(BigDecimal currentProgress) {
        this.currentProgress = currentProgress;
    }
    
    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }
    
    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
}
