package com.rag.chat.entity;

import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;

import com.rag.chat.model.AttributeEncryptor;
import com.rag.chat.model.Sender;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "chat_messages")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ChatMessage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sender sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String content;

    @Convert(converter = AttributeEncryptor.class)
    @Column(columnDefinition = "TEXT")
    private String context;

    private String sourceName;

    private String sourceType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @CreationTimestamp
    private Timestamp createdAt;
    
    @Version
    private Integer version;
}
