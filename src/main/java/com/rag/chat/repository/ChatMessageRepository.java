package com.rag.chat.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.rag.chat.entity.ChatMessage;
import com.rag.chat.entity.ChatSession;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.session.id = :sessionId")
    int deleteBySessionId(UUID sessionId);

    Page<ChatMessage> findBySessionOrderByCreatedAtAsc(ChatSession session, PageRequest pageable);
}
