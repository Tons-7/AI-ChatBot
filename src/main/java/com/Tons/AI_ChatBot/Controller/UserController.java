package com.Tons.AI_ChatBot.Controller;

import com.Tons.AI_ChatBot.DTO.ChangePasswordRequest;
import com.Tons.AI_ChatBot.DTO.UpdateUserRequest;
import com.Tons.AI_ChatBot.DTO.UserDTO;
import com.Tons.AI_ChatBot.DTO.UserSettingsDTO;
import com.Tons.AI_ChatBot.Entities.User;
import com.Tons.AI_ChatBot.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getProfile()
    {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(@RequestBody UpdateUserRequest request)
    {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount()
    {
        return ResponseEntity.ok(userService.deleteCurrentUser());
    }

    @PutMapping("/change_password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request)
    {
        return ResponseEntity.ok(userService.changePassword(request));
    }

    @PostMapping("forgot_password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email)
    {
        userService.sendPasswordReset(email);
        return ResponseEntity.ok("Reset link sent");
    }

    @PostMapping("reset_password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword)
    {
        return ResponseEntity.ok(userService.resetPassword(token, newPassword));
    }

    @GetMapping("/settings")
    public ResponseEntity<UserSettingsDTO> settings()
    {
        return ResponseEntity.ok(userService.getSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<UserSettingsDTO> updateSettings(@RequestBody UserSettingsDTO settings)
    {
        return ResponseEntity.ok(userService.updateSettings(settings));
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> avatar(@RequestParam("file") MultipartFile file)
    {
        return ResponseEntity.ok(userService.uploadAvatar(file));
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<String> deleteAvatar()
    {
        return ResponseEntity.ok("");
    }
}
