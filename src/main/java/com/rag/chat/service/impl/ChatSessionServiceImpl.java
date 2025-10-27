package com.rag.chat.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.rag.chat.common.audit.AuditAction;
import com.rag.chat.common.audit.Audited;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.exception.ChatSessionNotFoundException;
import com.rag.chat.exception.InvalidRequestException;
import com.rag.chat.mapper.ChatSessionMapper;
import com.rag.chat.model.ChatSessionRequest;
import com.rag.chat.model.ChatSessionResponse;
import com.rag.chat.repository.ChatMessageRepository;
import com.rag.chat.repository.ChatSessionRepository;
import com.rag.chat.service.ChatSessionService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionMapper chatSessionMapper;

    @Cacheable(value = "sessionsList", key = "'allSessions'")
    @Audited(action = AuditAction.SESSIONS_LIST, includeResponse = false)
    @Override
    public List<ChatSessionResponse> getAllSessions() {
        try {
            // Fetch all sessions sorted by creation date descending
            List<ChatSession> chatSessionList = chatSessionRepository.findAll(Sort.by("createdAt").descending());

            if (!CollectionUtils.isEmpty(chatSessionList)) {
                log.info("Retrieved {} chat sessions", chatSessionList.size());

                return chatSessionMapper.toResponseList(chatSessionList);
            } else {
                log.info("No chat sessions found");

                return List.of();
            }
        } catch (Exception e) {
            log.error("Unexpected error while retrieving sessions [{}]", e.getMessage(), e);
            throw e;
        }
    }

    @Cacheable(value = "sessions", key = "#sessionId")
    @Override
    public ChatSessionResponse getSessionById(UUID sessionId) {
        try {
            ChatSession chatSession = getChatSessionById(sessionId);
            log.info("Retrieved session [{}]", sessionId);

            return chatSessionMapper.toResponse(chatSession);

        } catch (ChatSessionNotFoundException e) {
            log.warn("GetSessionById validation error for session [{}]: {}", sessionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while retrieving session [{}]: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    @CacheEvict(value = {"sessionsList"}, allEntries = true)
    @Audited(action = AuditAction.SESSION_CREATE)
    @Override
    public ChatSessionResponse createSession(ChatSessionRequest chatSessionRequest) {
        try {
            // Transform to entity
            ChatSession chatSession = chatSessionMapper.toEntity(chatSessionRequest);

            // Save the session
            ChatSession savedSession = chatSessionRepository.save(chatSession);
            log.info("Created a new session [{}]", savedSession.getId());

            return chatSessionMapper.toResponse(savedSession);

        } catch (InvalidRequestException | ChatSessionNotFoundException e) {
            log.warn("CreateSession validation error for session [{}]: {}", chatSessionRequest.getName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating the new session [{}]: {}", chatSessionRequest.getName(), e.getMessage(), e);
            throw e;
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "sessions", key = "#sessionId"),
            @CacheEvict(value = "sessionsList", allEntries = true)
    })
    @Audited(action = AuditAction.SESSION_DELETE)
    @Override
    public void deleteSession(UUID sessionId) {
        try {
            // Verify if session exists
            if (!chatSessionRepository.existsById(sessionId)) {
                throw new ChatSessionNotFoundException("Chat session not found with id: " + sessionId);
            }

            // Delete messages associated with the session
            int deletedMessagesCount = chatMessageRepository.deleteBySessionId(sessionId);

            // Delete the session
            chatSessionRepository.deleteById(sessionId);
            log.info("Deleted session [{}] and {} messages", sessionId, deletedMessagesCount);

        } catch (ChatSessionNotFoundException e) {
            log.warn("DeleteSession validation error for session [{}]: {}", sessionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while deleting session [{}]: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    @Caching(put = {@CachePut(value = "sessions", key = "#sessionId")},
            evict = {@CacheEvict(value = "sessionsList", allEntries = true)}
    )
    @Audited(action = AuditAction.SESSION_RENAME)
    @Override
    public ChatSessionResponse renameSession(UUID sessionId, ChatSessionRequest chatSessionRequest) {
        try {
            // Fetch the session by ID and update its name
            ChatSession chatSession = getChatSessionById(sessionId);
            chatSession.setName(chatSessionRequest.getName());

            // Save the updated session
            ChatSession updatedSession = chatSessionRepository.save(chatSession);
            log.info("Renamed session [{}] to '{}'", sessionId, updatedSession.getName());

            return chatSessionMapper.toResponse(updatedSession);

        } catch (InvalidRequestException | ChatSessionNotFoundException e) {
            log.warn("RenameSession validation error for session [{}]: {}", sessionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while renaming session [{}]: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    @Caching(put = {@CachePut(value = "sessions", key = "#sessionId")},
            evict = {@CacheEvict(value = "sessionsList", allEntries = true)}
    )
    @Audited(action = AuditAction.SESSION_MARK_FAVORITE)
    @Override
    public ChatSessionResponse markAsFavorite(UUID sessionId) {
        try {
            // Fetch the session by ID and mark as favorite
            ChatSession chatSession = getChatSessionById(sessionId);
            chatSession.setFavorite(true);

            // Save the updated session
            ChatSession updatedSession = chatSessionRepository.save(chatSession);
            log.info("Marked session [{}] as favorite", sessionId);

            return chatSessionMapper.toResponse(updatedSession);

        } catch (ChatSessionNotFoundException e) {
            log.warn("MarkAsFavorite validation error for session [{}]: {}", sessionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while marking session [{}] as favorite: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    @Caching(put = {@CachePut(value = "sessions", key = "#sessionId")},
            evict = {@CacheEvict(value = "sessionsList", allEntries = true)}
    )
    @Audited(action = AuditAction.SESSION_UNMARK_FAVORITE)
    @Override
    public ChatSessionResponse unmarkAsFavorite(UUID sessionId) {
        try {
            // Fetch the session by ID and unmark as favorite
            ChatSession chatSession = getChatSessionById(sessionId);
            chatSession.setFavorite(false);

            // Save the updated session
            ChatSession updatedSession = chatSessionRepository.save(chatSession);
            log.info("UnmMarked session [{}] as favorite", sessionId);

            return chatSessionMapper.toResponse(updatedSession);

        } catch (ChatSessionNotFoundException e) {
            log.warn("UnmarkAsFavorite validation error for session [{}]: {}", sessionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while unmarking session [{}] as favorite: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    private ChatSession getChatSessionById(UUID sessionId) {
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ChatSessionNotFoundException("Chat session not found with id: " + sessionId));
    }
}

