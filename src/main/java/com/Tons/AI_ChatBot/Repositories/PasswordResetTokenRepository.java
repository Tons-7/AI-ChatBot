package com.Tons.AI_ChatBot.Repositories;

import com.Tons.AI_ChatBot.Entities.PasswordResetToken;
import com.Tons.AI_ChatBot.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}
