package com.example.skye.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.skye.dto.CategoryDto;
import com.example.skye.entity.Category;
import com.example.skye.entity.User;
import com.example.skye.exception.DuplicateResourceException;
import com.example.skye.exception.ResourceNotFoundException;
import com.example.skye.repository.CategoryRepository;
import com.example.skye.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "hash", "Jane", "+1234567890");
        user.setId(1L);
        customCategory = new Category("Gym", Category.CategoryType.EXPENSE, true, user);
        customCategory.setId(10L);
    }

    @Test
    @DisplayName("List default + custom categories for a user")
    void getCategoriesForUser() {
        Category salary = new Category("Salary", Category.CategoryType.INCOME, false, null);
        when(categoryRepository.findCategoriesAccessibleToUser(user)).thenReturn(List.of(salary, customCategory));

        List<CategoryDto> result = categoryService.getCategoriesForUser(user);

        assertEquals(2, result.size());
        assertEquals("Salary", result.get(0).getName());
        assertTrue(result.get(1).isCustom());
    }

    @Test
    @DisplayName("Create custom income/expense category")
    void createCustomCategory_Success() {
        CategoryDto dto = new CategoryDto("Freelance", "INCOME");
        when(categoryRepository.existsByNameForUser("Freelance", user)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(11L);
            return c;
        });

        CategoryDto saved = categoryService.createCustomCategory(dto, user);

        assertEquals("Freelance", saved.getName());
        assertEquals("INCOME", saved.getType());
        assertTrue(saved.isCustom());
    }

    @Test
    @DisplayName("Reject duplicate category name")
    void createCustomCategory_Duplicate() {
        CategoryDto dto = new CategoryDto("Food", "EXPENSE");
        when(categoryRepository.existsByNameForUser("Food", user)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCustomCategory(dto, user));
    }

    @Test
    @DisplayName("Cannot delete a default category")
    void deleteDefaultCategory_Blocked() {
        Category food = new Category("Food", Category.CategoryType.EXPENSE, false, null);
        when(categoryRepository.findByNameAndUser("Food", user)).thenReturn(Optional.of(food));

        assertThrows(RuntimeException.class, () -> categoryService.deleteCustomCategory("Food", user));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Cannot delete a category used by transactions")
    void deleteCategoryInUse_Blocked() {
        when(categoryRepository.findByNameAndUser("Gym", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> categoryService.deleteCustomCategory("Gym", user));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete unused custom category")
    void deleteCustomCategory_Success() {
        when(categoryRepository.findByNameAndUser("Gym", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(false);

        categoryService.deleteCustomCategory("Gym", user);

        verify(categoryRepository).delete(customCategory);
    }

    @Test
    @DisplayName("Unknown category name returns 404-style error")
    void deleteMissingCategory() {
        when(categoryRepository.findByNameAndUser("Nope", user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCustomCategory("Nope", user));
    }
}
