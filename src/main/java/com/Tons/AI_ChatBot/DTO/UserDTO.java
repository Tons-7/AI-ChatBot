package com.Tons.AI_ChatBot.DTO;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String avatar_url;
    private String language;
    private String theme;
}
