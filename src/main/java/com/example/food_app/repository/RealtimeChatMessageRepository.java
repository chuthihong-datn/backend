package com.example.food_app.repository;

import com.example.food_app.entity.RealtimeChatMessage;
import com.example.food_app.entity.RealtimeChatSession;
import com.example.food_app.entity.enums.RealtimeChatSenderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RealtimeChatMessageRepository extends JpaRepository<RealtimeChatMessage, Long> {
    List<RealtimeChatMessage> findBySessionOrderByCreatedAtAsc(RealtimeChatSession session);

    List<RealtimeChatMessage> findBySessionAndSenderTypeAndReadByCustomerFalse(
            RealtimeChatSession session,
            RealtimeChatSenderType senderType
    );

    List<RealtimeChatMessage> findBySessionAndSenderTypeInAndReadByAdminFalse(
            RealtimeChatSession session,
            Collection<RealtimeChatSenderType> senderTypes
    );
}
