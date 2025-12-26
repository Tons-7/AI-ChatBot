package com.Tons.AI_ChatBot.Repositories;

import com.Tons.AI_ChatBot.Entities.Conversation;
import com.Tons.AI_ChatBot.Entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>{
    List<Message> findByConversationOrderByTimestampAsc(Conversation conversation);
}
