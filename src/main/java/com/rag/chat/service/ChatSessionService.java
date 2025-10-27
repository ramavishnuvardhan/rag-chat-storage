package com.rag.chat.service;

import java.util.List;
import java.util.UUID;

import com.rag.chat.model.ChatSessionRequest;
import com.rag.chat.model.ChatSessionResponse;

public interface ChatSessionService {

    ChatSessionResponse createSession(ChatSessionRequest chatSessionRequest);

    void deleteSession(UUID sessionId);

    ChatSessionResponse renameSession(UUID sessionId, ChatSessionRequest chatSessionRequest);

    ChatSessionResponse markAsFavorite(UUID sessionId);

    ChatSessionResponse unmarkAsFavorite(UUID sessionId);

    List<ChatSessionResponse> getAllSessions();

    ChatSessionResponse getSessionById(UUID sessionId);
}
