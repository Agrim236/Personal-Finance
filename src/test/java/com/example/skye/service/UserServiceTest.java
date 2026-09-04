package com.example.skye.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.skye.dto.UserLoginDto;
import com.example.skye.dto.UserRegistrationDto;
import com.example.skye.entity.User;
import com.example.skye.exception.DuplicateResourceException;
import com.example.skye.exception.InvalidCredentialsException;
import com.example.skye.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto registrationDto;
    private UserLoginDto loginDto;
    private User user;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto(
                "test@example.com", "password123", "John Doe", "+1234567890");
        loginDto = new UserLoginDto("test@example.com", "password123");
        user = new User("test@example.com", "encodedPassword", "John Doe", "+1234567890");
        user.setId(1L);
    }

    @Test
    @DisplayName("Register user with email, password, full name, phone")
    void registerUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser(registrationDto);

        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Reject duplicate username (email)")
    void registerUser_DuplicateUsername() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(registrationDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login with valid credentials")
    void authenticateUser_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        User result = userService.authenticateUser(loginDto);

        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Login fails for unknown username")
    void authenticateUser_InvalidUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.authenticateUser(loginDto));
    }

    @Test
    @DisplayName("Login fails for wrong password")
    void authenticateUser_InvalidPassword() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.authenticateUser(loginDto));
    }
}
