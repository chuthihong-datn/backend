package com.example.food_app.entity;

import com.example.food_app.entity.enums.RealtimeChatSessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "realtime_chat_sessions",
        indexes = {
                @Index(name = "idx_realtime_chat_session_public_id", columnList = "public_id"),
                @Index(name = "idx_realtime_chat_session_guest_hash", columnList = "guest_token_hash")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RealtimeChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_session_id")
    private Long chatSessionId;

    @Column(name = "public_id", nullable = false, unique = true, length = 64)
    private String publicId;

    @Column(name = "guest_token_hash", unique = true, length = 128)
    private String guestTokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RealtimeChatSessionStatus status = RealtimeChatSessionStatus.OPEN;

    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "customer_unread_count", nullable = false)
    private Integer customerUnreadCount = 0;

    @Column(name = "admin_unread_count", nullable = false)
    private Integer adminUnreadCount = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RealtimeChatMessage> messages;
}
