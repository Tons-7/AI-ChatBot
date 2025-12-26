package com.Tons.AI_ChatBot.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@Data
@Entity
@Table(name = "conversations")
public class Conversation
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long conversation_id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "conversation",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<Message> messages;

    public Conversation(String title, User user)
    {
        this.title = title;
        this.user = user;
    }
}
