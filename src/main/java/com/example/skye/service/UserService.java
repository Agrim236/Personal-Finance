package com.example.skye.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skye.dto.UserLoginDto;
import com.example.skye.dto.UserRegistrationDto;
import com.example.skye.entity.User;
import com.example.skye.exception.DuplicateResourceException;
import com.example.skye.exception.InvalidCredentialsException;
import com.example.skye.repository.UserRepository;

/**
 * Service class for user management and authentication
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
   
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        // Phone number uniqueness check removed
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        return userRepository.save(user);
    }
    
 
    public User authenticateUser(UserLoginDto loginDto) {
        User user = userRepository.findByUsername(loginDto.getUsername())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        
        return user;
    }
    
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
   
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
