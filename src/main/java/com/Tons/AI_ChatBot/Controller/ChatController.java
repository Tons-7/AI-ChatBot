package com.Tons.AI_ChatBot.Controller;

import com.Tons.AI_ChatBot.DTO.MessageRequest;
import com.Tons.AI_ChatBot.DTO.MessageResponse;
import com.Tons.AI_ChatBot.Entities.Conversation;
import com.Tons.AI_ChatBot.Entities.Message;
import com.Tons.AI_ChatBot.Services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/")
    public String mainPage() {
        return "Main Chat Page";
    }

    @PostMapping("/message")
    public MessageResponse sendMessage(@RequestBody MessageRequest request) {
        return chatService.getResponse(request);
    }

    @PostMapping("/conversation")
    public Conversation startConversation(@RequestParam String title, Principal principal) {
        return chatService.startConversation(title, principal.getName());
    }

    @GetMapping("/conversation/{id}/messages")
    public List<Message> getConversationMessages(@PathVariable Long id) {
        return chatService.getConversationMessages(id);
    }

    @GetMapping("/my_conversations")
    public List<Conversation> getUserConversations(Principal principal) {
        return chatService.getUserConversations(principal.getName());
    }
}
