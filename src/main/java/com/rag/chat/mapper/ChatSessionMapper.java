package com.rag.chat.mapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.rag.chat.entity.ChatSession;
import com.rag.chat.model.ChatSessionRequest;
import com.rag.chat.model.ChatSessionResponse;

@Component
public class ChatSessionMapper {

	public ChatSession toEntity(ChatSessionRequest request) {
		ChatSession chatSession = new ChatSession();
		chatSession.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
		chatSession.setFavorite(false);
		chatSession.setName(request.getName());
		chatSession.setMessages(null);

		return chatSession;
	}

	public ChatSessionResponse toResponse(ChatSession entity) {
		ChatSessionResponse chatSessionResponse = new ChatSessionResponse();
		chatSessionResponse.setCreatedAt(entity.getCreatedAt());
		chatSessionResponse.setFavorite(entity.isFavorite());
		chatSessionResponse.setName(entity.getName());
		chatSessionResponse.setId(entity.getId());
		return chatSessionResponse;

	}

	public List<ChatSessionResponse> toResponseList(List<ChatSession> entities) {
		List<ChatSessionResponse> list = new ArrayList<>();
		entities.stream().forEach(k -> list.add(toResponse(k)));
		return list;

	}
}
