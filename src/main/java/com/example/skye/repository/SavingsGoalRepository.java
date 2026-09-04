package com.example.skye.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.skye.entity.SavingsGoal;
import com.example.skye.entity.User;


@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserOrderByCreatedAtDesc(User user);

    SavingsGoal findByIdAndUser(Long id, User user);
}
