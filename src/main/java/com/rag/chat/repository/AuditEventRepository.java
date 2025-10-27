package com.rag.chat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rag.chat.entity.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
}
