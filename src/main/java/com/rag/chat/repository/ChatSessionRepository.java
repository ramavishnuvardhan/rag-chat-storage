package com.rag.chat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rag.chat.entity.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
}
