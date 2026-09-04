package com.example.skye.config;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.skye.entity.User;
import com.example.skye.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;

    public SessionAuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            Long userId = (Long) session.getAttribute("userId");
            String username = (String) session.getAttribute("username");
            
            if (userId != null && username != null) {
                try {
                    User user = userService.findById(userId);
                    
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, 
                        null, 
                        new ArrayList<>() 
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                } catch (Exception e) {
                    session.invalidate();
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
} 