package com.Tons.AI_ChatBot.DTO;

public record AuthResponse(
        String token,
        String username,
        String email,
        String message
) {}

