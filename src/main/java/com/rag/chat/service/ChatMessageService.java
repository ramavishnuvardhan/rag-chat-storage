package com.rag.chat.service;

import java.util.List;
import java.util.UUID;

import com.rag.chat.model.ChatMessageRequest;
import com.rag.chat.model.ChatMessageResponse;

public interface ChatMessageService {

    ChatMessageResponse addMessage(UUID sessionId, ChatMessageRequest request);

    List<ChatMessageResponse> getMessagesBySessionId(UUID sessionId, int page, int size);

    void deleteMessageById(UUID messageId);

    int deleteMessagesBySessionId(UUID sessionId);
}
