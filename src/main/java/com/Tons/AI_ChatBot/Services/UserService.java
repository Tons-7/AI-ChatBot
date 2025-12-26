package com.Tons.AI_ChatBot.Services;

import com.Tons.AI_ChatBot.DTO.UserSettingsDTO;
import com.Tons.AI_ChatBot.DTO.ChangePasswordRequest;
import com.Tons.AI_ChatBot.DTO.UserDTO;
import com.Tons.AI_ChatBot.DTO.UpdateUserRequest;

import com.Tons.AI_ChatBot.Entities.PasswordResetToken;
import com.Tons.AI_ChatBot.Entities.User;

import com.Tons.AI_ChatBot.Repositories.PasswordResetTokenRepository;
import com.Tons.AI_ChatBot.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;


    public UserDTO getCurrentUserProfile() {
        String email = authService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserDTO dto = new UserDTO();
        dto.setId(user.getUser_id());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatar_url(user.getAvatar_url());
        dto.setLanguage(user.getLanguage());
        dto.setTheme(user.getTheme());

        return dto;
    }

    public UserDTO updateUser(UpdateUserRequest request)
    {
        String email = authService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getUsername() != null && !request.getUsername().isBlank())
            user.setUsername(request.getUsername());

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank())
            user.setAvatar_url(request.getAvatarUrl());

        if (request.getLanguage() != null && !request.getLanguage().isBlank())
            user.setLanguage(request.getLanguage());

        if (request.getTheme() != null && !request.getTheme().isBlank())
            user.setTheme(request.getTheme());

        userRepository.save(user);

        UserDTO dto = new UserDTO();

        dto.setId(user.getUser_id());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatar_url(user.getAvatar_url());
        dto.setLanguage(user.getLanguage());
        dto.setTheme(user.getTheme());

        return dto;
    }

    public String deleteCurrentUser()
    {
        String email = authService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);

        return "Account deleted successfully";
    }

    public String changePassword(ChangePasswordRequest request)
    {
        if (request == null || request.getOldPassword() == null || request.getNewPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }

        String email = authService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            return "Old password is incorrect";

        if (request.getNewPassword().equals(request.getOldPassword()))
            return "Your new password can't be the same as your old password";

        if (request.getNewPassword().isBlank() || request.getNewPassword().length() < 5)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is invalid (minimum 5 chars)");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        return "Password changed successfully";
    }

    public void sendPasswordReset(String email)
    {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();

        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpirationDate(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepository.save(resetToken);

        // Add email things here later
    }

    public String resetPassword(String token, String newPassword)
    {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now()))
        {
            passwordResetTokenRepository.delete(resetToken);
            throw new ResponseStatusException(HttpStatus.GONE, "Token expired");
        }

        User user = resetToken.getUser();

        if (newPassword.length() < 5)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 5 characters");

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        return "Password has been reset successfully";
    }

    public UserSettingsDTO getSettings()
    {
        String email = authService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserSettingsDTO dto = new UserSettingsDTO();

        dto.setTheme(user.getTheme());
        dto.setLanguage(user.getLanguage());

        return dto;
    }

    public UserSettingsDTO updateSettings(UserSettingsDTO settings)
    {
        String email = authService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (settings.getTheme() != null && !settings.getTheme().isBlank())
            user.setTheme(settings.getTheme());

        if (settings.getLanguage() != null && !settings.getLanguage().isBlank())
            user.setLanguage(settings.getLanguage());

        userRepository.save(user);

        UserSettingsDTO updated = new UserSettingsDTO();

        updated.setTheme(user.getTheme());
        updated.setLanguage(user.getLanguage());

        return updated;
    }

    public String uploadAvatar(MultipartFile file)
    {
        String email = authService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");

        try {
            Path upload_dir = Path.of("uploads", "avatars");
            Files.createDirectories(upload_dir);

            String original = file.getOriginalFilename();
            String extension = "";

            if (original != null && original.contains("."))
                extension = original.substring(original.lastIndexOf("."));


            String filename = UUID.randomUUID() + extension;
            Path filePath = upload_dir.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/uploads/avatars/" + filename;
            user.setAvatar_url(avatarUrl);

            userRepository.save(user);

            return avatarUrl;

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading avatar", e);
        }
    }

    public String resetAvatar()
    {
        String email = authService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getAvatar_url() != null && user.getAvatar_url().startsWith("/uploads/"))
        {

            try{
                Path old_avatar_path = Path.of(user.getAvatar_url().substring(1));

                Files.deleteIfExists(old_avatar_path);
            } catch (IOException e){
                System.err.println("Failed to delete old avatar: " + e.getMessage());
            }
        }

        user.setAvatar_url("/images/default_image.jpg");

        userRepository.save(user);

        return "Avatar has been reset to default";
    }
}
