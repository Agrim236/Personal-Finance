package com.example.skye.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.skye.entity.Category;
import com.example.skye.entity.User;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    

    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user = :user")
    List<Category> findCategoriesAccessibleToUser(@Param("user") User user);
    
  
    List<Category> findByUserAndIsCustomTrue(User user);
    
  
    List<Category> findByUserIsNullAndIsCustomFalse();

    Optional<Category> findByNameAndUser(String name, User user);

    Optional<Category> findByNameAndUserIsNull(String name);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND (c.user = :user OR c.user IS NULL)")
    boolean existsByNameForUser(@Param("name") String name, @Param("user") User user);
}
