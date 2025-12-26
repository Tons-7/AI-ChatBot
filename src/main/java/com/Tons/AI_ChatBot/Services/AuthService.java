package com.Tons.AI_ChatBot.Services;

import com.Tons.AI_ChatBot.DTO.AuthResponse;
import com.Tons.AI_ChatBot.DTO.AuthRequest;
import com.Tons.AI_ChatBot.DTO.RegisterRequest;

import com.Tons.AI_ChatBot.Entities.User;

import com.Tons.AI_ChatBot.Repositories.UserRepository;

import com.Tons.AI_ChatBot.Security.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    public AuthResponse register(RegisterRequest request)
    {
        if (userRepository.findByEmail(request.email()).isPresent())
        {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        user.setAvatar_url("images/default_image.jpg");

        userRepository.save(user);

        String jwtToken = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(
                jwtToken,
                user.getUsername(),
                user.getEmail(),
                "Registration Successful");
    }

    public AuthResponse login(AuthRequest request)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String jwtToken = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(
                jwtToken,
                user.getUsername(),
                user.getEmail(),
                "Login successful"
        );
    }
}
