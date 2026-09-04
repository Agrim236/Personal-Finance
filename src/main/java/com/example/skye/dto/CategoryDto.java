package com.example.skye.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class CategoryDto {
    
    private Long id;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    @NotNull(message = "Category type is required")
    private String type; 
    
    private boolean isCustom;
    
    public CategoryDto() {}
    
    public CategoryDto(String name, String type) {
        this.name = name;
        this.type = type;
        this.isCustom = true;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isCustom() {
        return isCustom;
    }
    
    public void setCustom(boolean custom) {
        isCustom = custom;
    }
}
