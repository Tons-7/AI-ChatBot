package com.Tons.AI_ChatBot.Services;

import com.Tons.AI_ChatBot.DTO.MessageRequest;
import com.Tons.AI_ChatBot.DTO.MessageResponse;

import com.Tons.AI_ChatBot.Entities.Conversation;
import com.Tons.AI_ChatBot.Entities.Message;
import com.Tons.AI_ChatBot.Entities.User;

import com.Tons.AI_ChatBot.Repositories.ConversationRepository;
import com.Tons.AI_ChatBot.Repositories.MessageRepository;
import com.Tons.AI_ChatBot.Repositories.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final RestClient restClient;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatService(RestClient.Builder builder, ConversationRepository conversationRepository, MessageRepository messageRepository, UserRepository userRepository)
    {
        this.restClient = builder
                .baseUrl("https://auditorily-nonparabolic-roseann.ngrok-free.dev")
                .build();
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public Conversation startConversation(String title, String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = new Conversation(title, user);
        return conversationRepository.save(conversation);
    }

    public List<Message> getConversationMessages(Long conversationId)
    {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return conversation.getMessages();
    }

    public List<Conversation> getUserConversations(String username)
    {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getConversations();
    }

    private void saveMessage(Long conversationId, String content, String role, User sender)
    {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setContent(content);
        message.setRole(role);
        message.setSender(sender);

        messageRepository.save(message);
    }

    public MessageResponse getResponse(MessageRequest request)
    {
        Long conversationId = request.conversation_id();
        String userInput = request.user_input();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        User sender = conversation.getUser();

        saveMessage(conversationId, userInput, "user", sender);

        List<Message> messages = messageRepository.findByConversationOrderByTimestampAsc(conversation);

        List<Map<String, String>> history = new ArrayList<>();

        for (Message msg : messages)
        {
            history.add(
                    Map.of(
                    "role", msg.getRole(),
                    "content", msg.getContent()
                    )
            );
        }

        Map<String, Object> body = Map.of(
                "conversation_id", conversationId,
                "history", history,
                "user_input", userInput
        );

        MessageResponse aiResponse;
        try {
            aiResponse = restClient.post()
                    .uri("/api/ai_response")
                    .body(body)
                    .retrieve()
                    .body(MessageResponse.class);
        } catch (Exception e) {
            aiResponse = new MessageResponse("AI service is unavailable right now");
        }

        assert aiResponse != null;
        saveMessage(conversationId, aiResponse.content(), "assistant", null);

        return aiResponse;
    }

}