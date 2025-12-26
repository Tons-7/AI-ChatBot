package com.Tons.AI_ChatBot.Entities;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

}

