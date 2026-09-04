package com.example.skye.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skye.dto.CategoryDto;
import com.example.skye.entity.Category;
import com.example.skye.entity.User;
import com.example.skye.exception.DuplicateResourceException;
import com.example.skye.exception.ResourceNotFoundException;
import com.example.skye.repository.CategoryRepository;
import com.example.skye.repository.TransactionRepository;

import jakarta.annotation.PostConstruct;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    

    @PostConstruct
    public void initializeDefaultCategories() {
        if (categoryRepository.findByUserIsNullAndIsCustomFalse().isEmpty()) {
            categoryRepository.save(new Category("Salary", Category.CategoryType.INCOME, false, null));
            
            categoryRepository.save(new Category("Food", Category.CategoryType.EXPENSE, false, null));
            categoryRepository.save(new Category("Rent", Category.CategoryType.EXPENSE, false, null));
            categoryRepository.save(new Category("Transportation", Category.CategoryType.EXPENSE, false, null));
            categoryRepository.save(new Category("Entertainment", Category.CategoryType.EXPENSE, false, null));
            categoryRepository.save(new Category("Healthcare", Category.CategoryType.EXPENSE, false, null));
            categoryRepository.save(new Category("Utilities", Category.CategoryType.EXPENSE, false, null));
        }
    }
    
    public List<CategoryDto> getCategoriesForUser(User user) {
        List<Category> categories = categoryRepository.findCategoriesAccessibleToUser(user);
        return categories.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    

    public CategoryDto createCustomCategory(CategoryDto categoryDto, User user) {
        if (categoryRepository.existsByNameForUser(categoryDto.getName(), user)) {
            throw new DuplicateResourceException("Category name already exists");
        }
        
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setType(Category.CategoryType.valueOf(categoryDto.getType()));
        category.setCustom(true);
        category.setUser(user);
        
        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }
    

    public void deleteCustomCategory(String categoryName, User user) {
        Category category = categoryRepository.findByNameAndUser(categoryName, user)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        if (!category.isCustom()) {
            throw new RuntimeException("Cannot delete default category");
        }
        
        if (transactionRepository.existsByCategory(category)) {
            throw new RuntimeException("Cannot delete category that is referenced by transactions");
        }
        
        categoryRepository.delete(category);
    }
    
 
    public Category findCategoryByName(String name, User user) {
        return categoryRepository.findByNameAndUser(name, user)
            .orElseGet(() -> categoryRepository.findByNameAndUserIsNull(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name)));
    }
    
    
    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType().toString());
        dto.setCustom(category.isCustom());
        return dto;
    }
}
