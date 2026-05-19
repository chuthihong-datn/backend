package com.example.food_app.entity;

import com.example.food_app.entity.enums.RealtimeChatSenderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "realtime_chat_messages",
        indexes = {
                @Index(name = "idx_realtime_chat_message_session_created", columnList = "chat_session_id, created_at")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RealtimeChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long chatMessageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_session_id", nullable = false)
    private RealtimeChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private RealtimeChatSenderType senderType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id")
    private Account senderAccount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "read_by_customer", nullable = false)
    private Boolean readByCustomer = false;

    @Column(name = "read_by_admin", nullable = false)
    private Boolean readByAdmin = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
