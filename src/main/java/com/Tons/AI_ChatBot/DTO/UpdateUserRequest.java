package com.Tons.AI_ChatBot.DTO;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String avatarUrl;
    private String language;
    private String theme;
}
