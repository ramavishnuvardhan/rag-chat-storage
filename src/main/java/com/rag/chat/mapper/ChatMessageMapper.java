package com.rag.chat.mapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.rag.chat.entity.ChatMessage;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.model.ChatMessageRequest;
import com.rag.chat.model.ChatMessageResponse;

@Component
public class ChatMessageMapper {

 
    public ChatMessage toEntity(ChatMessageRequest request) {
    	ChatMessage chatMessage = new ChatMessage();
    	chatMessage.setContent(request.getContent());
    	chatMessage.setContext(request.getContext());
    	chatMessage.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    	chatMessage.setSender(request.getSender());
    	chatMessage.setSourceName(request.getSourceName());
    	chatMessage.setSession(new ChatSession());
    	chatMessage.setSourceType(request.getSourceType());
        return chatMessage;
    }


    public ChatMessageResponse toResponse(ChatMessage entity) {
    	ChatMessageResponse ChatMessageResponse = new ChatMessageResponse();
    	ChatMessageResponse.setContent(entity.getContent());
    	ChatMessageResponse.setContext(entity.getContext());
    	ChatMessageResponse.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    	ChatMessageResponse.setId(entity.getId());
    	ChatMessageResponse.setSourceName(entity.getSourceName());
    	ChatMessageResponse.setSourceType(entity.getSourceType());
		return ChatMessageResponse;
    	
    }

    public List<ChatMessageResponse> toResponseList(List<ChatMessage> entities){
    	 List<ChatMessageResponse> list = new ArrayList<>();    	
    	entities.stream().forEach(k -> list.add(toResponse(k)));    	
    	return list;
    }
}
