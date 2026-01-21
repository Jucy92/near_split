package com.nearsplit.domain.chat.repository;

import com.nearsplit.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 그룹의 메시지 조회 (페이징)
    Page<ChatMessage> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    // 특정 그룹의 최근 메시지 N개 조회
    List<ChatMessage> findTop50ByGroupIdOrderByCreatedAtDesc(Long groupId);
}
