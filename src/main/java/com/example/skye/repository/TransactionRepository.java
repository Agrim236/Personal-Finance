package com.example.skye.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.skye.entity.Category;
import com.example.skye.entity.Transaction;
import com.example.skye.entity.User;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    
    List<Transaction> findByUserOrderByDateDescCreatedAtDesc(User user);
    
   
    List<Transaction> findByUserAndDateBetweenOrderByDateDescCreatedAtDesc(
        User user, LocalDate startDate, LocalDate endDate);
    
  
    List<Transaction> findByUserAndCategoryOrderByDateDescCreatedAtDesc(User user, Category category);
    
  
    List<Transaction> findByUserAndCategoryAndDateBetweenOrderByDateDescCreatedAtDesc(
        User user, Category category, LocalDate startDate, LocalDate endDate);
    
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = 'INCOME' AND t.date >= :startDate")
    BigDecimal calculateTotalIncomeSince(@Param("user") User user, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = 'EXPENSE' AND t.date >= :startDate")
    BigDecimal calculateTotalExpensesSince(@Param("user") User user, @Param("startDate") LocalDate startDate);
    
   
    @Query("SELECT c.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN t.category c WHERE t.user = :user AND c.type = 'INCOME' " +
           "AND t.date BETWEEN :startDate AND :endDate GROUP BY c.name")
    List<Object[]> getIncomeByCategory(@Param("user") User user, 
                                      @Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
  
    @Query("SELECT c.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN t.category c WHERE t.user = :user AND c.type = 'EXPENSE' " +
           "AND t.date BETWEEN :startDate AND :endDate GROUP BY c.name")
    List<Object[]> getExpensesByCategory(@Param("user") User user, 
                                        @Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    boolean existsByCategory(Category category);
}
