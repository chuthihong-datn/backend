package com.example.food_app.repository;

import com.example.food_app.entity.Account;
import com.example.food_app.entity.RealtimeChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RealtimeChatSessionRepository extends JpaRepository<RealtimeChatSession, Long> {
    Optional<RealtimeChatSession> findByPublicId(String publicId);

    Optional<RealtimeChatSession> findByGuestTokenHash(String guestTokenHash);

    Optional<RealtimeChatSession> findFirstByAccountOrderByUpdatedAtDesc(Account account);

    List<RealtimeChatSession> findAllByOrderByUpdatedAtDesc();
}
