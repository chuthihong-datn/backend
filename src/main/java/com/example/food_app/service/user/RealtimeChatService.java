package com.example.food_app.service.user;

import com.example.food_app.dto.request.user.RealtimeChatMessageRequest;
import com.example.food_app.dto.request.user.RealtimeChatTypingRequest;
import com.example.food_app.dto.response.user.RealtimeChatMessageResponse;
import com.example.food_app.dto.response.user.RealtimeChatSessionResponse;
import com.example.food_app.dto.response.user.RealtimeChatTypingResponse;
import com.example.food_app.entity.Account;
import com.example.food_app.entity.RealtimeChatMessage;
import com.example.food_app.entity.RealtimeChatSession;
import com.example.food_app.entity.enums.RealtimeChatSenderType;
import com.example.food_app.entity.enums.RealtimeChatSessionStatus;
import com.example.food_app.repository.RealtimeChatMessageRepository;
import com.example.food_app.repository.RealtimeChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RealtimeChatService {
    private static final int MAX_CONTENT_LENGTH = 1000;
    private static final int PREVIEW_LENGTH = 255;
    private static final String ADMIN_TOPIC = "/topic/admin/realtime-chat";

    private final RealtimeChatSessionRepository sessionRepository;
    private final RealtimeChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public RealtimeChatSessionResponse createOrGetSession(Account account, String guestToken) {
        String normalizedGuestToken = normalizeToken(guestToken);
        String guestTokenHash = hashGuestToken(normalizedGuestToken);

        if (account != null && guestTokenHash != null) {
            RealtimeChatSession guestSession = sessionRepository.findByGuestTokenHash(guestTokenHash)
                    .map(session -> attachAccountIfNeeded(session, account))
                    .orElse(null);

            if (guestSession != null) {
                return mapSession(guestSession, null);
            }
        }

        if (account != null) {
            return sessionRepository.findFirstByAccountOrderByUpdatedAtDesc(account)
                    .map(session -> mapSession(session, null))
                    .orElseGet(() -> {
                        RealtimeChatSession session = createSession(account, null);
                        return mapSession(session, null);
                    });
        }

        if (guestTokenHash != null) {
            return sessionRepository.findByGuestTokenHash(guestTokenHash)
                    .map(session -> mapSession(session, null))
                    .orElseGet(() -> {
                        String newGuestToken = generateGuestToken();
                        RealtimeChatSession session = createSession(null, newGuestToken);
                        return mapSession(session, newGuestToken);
                    });
        }

        String newGuestToken = generateGuestToken();
        RealtimeChatSession session = createSession(null, newGuestToken);
        return mapSession(session, newGuestToken);
    }

    @Transactional(readOnly = true)
    public List<RealtimeChatMessageResponse> getMessages(
            String publicId,
            Account account,
            String guestToken
    ) {
        RealtimeChatSession session = getParticipantSession(publicId, account, guestToken);
        return messageRepository.findBySessionOrderByCreatedAtAsc(session)
                .stream()
                .map(this::mapMessage)
                .toList();
    }

    @Transactional
    public RealtimeChatMessageResponse sendParticipantMessage(
            String publicId,
            Account account,
            RealtimeChatMessageRequest request
    ) {
        String content = normalizeContent(request != null ? request.getContent() : null);
        String guestToken = request != null ? request.getGuestToken() : null;
        RealtimeChatSession session = getParticipantSession(publicId, account, guestToken);

        if (session.getStatus() == RealtimeChatSessionStatus.CLOSED) {
            session.setStatus(RealtimeChatSessionStatus.OPEN);
        }

        RealtimeChatMessage message = new RealtimeChatMessage();
        message.setSession(session);
        message.setSenderType(account != null ? RealtimeChatSenderType.CUSTOMER : RealtimeChatSenderType.GUEST);
        message.setSenderAccount(account);
        message.setContent(content);
        message.setReadByCustomer(true);
        message.setReadByAdmin(false);

        touchSessionForMessage(session, content);
        session.setAdminUnreadCount(safeCount(session.getAdminUnreadCount()) + 1);

        RealtimeChatMessage saved = messageRepository.save(message);
        sessionRepository.save(session);

        RealtimeChatMessageResponse response = mapMessage(saved);
        broadcastMessageAndSession(session, response);
        return response;
    }

    @Transactional
    public void markParticipantRead(String publicId, Account account, String guestToken) {
        RealtimeChatSession session = getParticipantSession(publicId, account, guestToken);
        List<RealtimeChatMessage> unreadMessages =
                messageRepository.findBySessionAndSenderTypeAndReadByCustomerFalse(
                        session,
                        RealtimeChatSenderType.ADMIN
                );

        unreadMessages.forEach(message -> message.setReadByCustomer(true));
        messageRepository.saveAll(unreadMessages);
        session.setCustomerUnreadCount(0);
        sessionRepository.save(session);
        broadcastReadReceipts(session, unreadMessages);
        messagingTemplate.convertAndSend(ADMIN_TOPIC, mapSession(session, null));
    }

    @Transactional(readOnly = true)
    public List<RealtimeChatSessionResponse> getAdminSessions() {
        return sessionRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(session -> mapSession(session, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RealtimeChatMessageResponse> getAdminMessages(String publicId) {
        RealtimeChatSession session = findSession(publicId);
        return messageRepository.findBySessionOrderByCreatedAtAsc(session)
                .stream()
                .map(this::mapMessage)
                .toList();
    }

    @Transactional
    public RealtimeChatMessageResponse sendAdminMessage(
            String publicId,
            Account admin,
            RealtimeChatMessageRequest request
    ) {
        String content = normalizeContent(request != null ? request.getContent() : null);
        RealtimeChatSession session = findSession(publicId);

        RealtimeChatMessage message = new RealtimeChatMessage();
        message.setSession(session);
        message.setSenderType(RealtimeChatSenderType.ADMIN);
        message.setSenderAccount(admin);
        message.setContent(content);
        message.setReadByCustomer(false);
        message.setReadByAdmin(true);

        touchSessionForMessage(session, content);
        session.setCustomerUnreadCount(safeCount(session.getCustomerUnreadCount()) + 1);

        RealtimeChatMessage saved = messageRepository.save(message);
        sessionRepository.save(session);

        RealtimeChatMessageResponse response = mapMessage(saved);
        broadcastMessageAndSession(session, response);
        return response;
    }

    @Transactional
    public void markAdminRead(String publicId) {
        RealtimeChatSession session = findSession(publicId);
        List<RealtimeChatMessage> unreadMessages =
                messageRepository.findBySessionAndSenderTypeInAndReadByAdminFalse(
                        session,
                        List.of(RealtimeChatSenderType.CUSTOMER, RealtimeChatSenderType.GUEST)
                );

        unreadMessages.forEach(message -> message.setReadByAdmin(true));
        messageRepository.saveAll(unreadMessages);
        session.setAdminUnreadCount(0);
        sessionRepository.save(session);
        broadcastReadReceipts(session, unreadMessages);
        messagingTemplate.convertAndSend(ADMIN_TOPIC, mapSession(session, null));
    }

    @Transactional
    public RealtimeChatSessionResponse updateStatus(String publicId, RealtimeChatSessionStatus status) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái là bắt buộc");
        }

        RealtimeChatSession session = findSession(publicId);
        session.setStatus(status);
        sessionRepository.save(session);

        RealtimeChatSessionResponse response = mapSession(session, null);
        messagingTemplate.convertAndSend(ADMIN_TOPIC, response);
        return response;
    }

    @Transactional(readOnly = true)
    public void publishTyping(String publicId, RealtimeChatTypingRequest request) {
        RealtimeChatSession session = findSession(publicId);
        RealtimeChatSenderType senderType = normalizeTypingSenderType(session, request);
        boolean typing = request != null && Boolean.TRUE.equals(request.getTyping());

        RealtimeChatTypingResponse response = RealtimeChatTypingResponse.builder()
                .sessionPublicId(session.getPublicId())
                .senderType(senderType)
                .senderName(getSenderName(session, senderType))
                .typing(typing)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/realtime-chat/" + session.getPublicId() + "/typing",
                response
        );
    }

    private RealtimeChatSession createSession(Account account, String rawGuestToken) {
        RealtimeChatSession session = new RealtimeChatSession();
        session.setPublicId(UUID.randomUUID().toString());
        session.setAccount(account);
        session.setGuestTokenHash(hashGuestToken(rawGuestToken));
        session.setStatus(RealtimeChatSessionStatus.OPEN);
        session.setCustomerUnreadCount(0);
        session.setAdminUnreadCount(0);
        return sessionRepository.save(session);
    }

    private RealtimeChatSession attachAccountIfNeeded(RealtimeChatSession session, Account account) {
        if (session.getAccount() == null) {
            session.setAccount(account);
            return sessionRepository.save(session);
        }

        if (sameAccount(session.getAccount(), account)) {
            return session;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Phiên chat thuộc về tài khoản khác");
    }

    private RealtimeChatSession getParticipantSession(
            String publicId,
            Account account,
            String guestToken
    ) {
        RealtimeChatSession session = findSession(publicId);

        if (account != null) {
            if (session.getAccount() == null) {
                String guestTokenHash = hashGuestToken(guestToken);
                if (guestTokenHash != null && guestTokenHash.equals(session.getGuestTokenHash())) {
                    return attachAccountIfNeeded(session, account);
                }
            }

            if (sameAccount(session.getAccount(), account)) {
                return session;
            }

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập phiên chat này");
        }

        String guestTokenHash = hashGuestToken(guestToken);
        if (guestTokenHash != null && guestTokenHash.equals(session.getGuestTokenHash())) {
            return session;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Phiên chat khách không hợp lệ");
    }

    private RealtimeChatSession findSession(String publicId) {
        return sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy phiên chat"
                ));
    }

    private void touchSessionForMessage(RealtimeChatSession session, String content) {
        session.setLastMessagePreview(trimPreview(content));
        session.setLastMessageAt(LocalDateTime.now());
    }

    private void broadcastMessageAndSession(
            RealtimeChatSession session,
            RealtimeChatMessageResponse message
    ) {
        messagingTemplate.convertAndSend(
                "/topic/realtime-chat/" + session.getPublicId(),
                message
        );
        messagingTemplate.convertAndSend(ADMIN_TOPIC, mapSession(session, null));
    }

    private void broadcastReadReceipts(
            RealtimeChatSession session,
            List<RealtimeChatMessage> messages
    ) {
        String topic = "/topic/realtime-chat/" + session.getPublicId();
        messages.stream()
                .map(this::mapMessage)
                .forEach(message -> messagingTemplate.convertAndSend(topic, message));
    }

    private RealtimeChatSessionResponse mapSession(RealtimeChatSession session, String newGuestToken) {
        Account account = session.getAccount();
        boolean guest = account == null;

        return RealtimeChatSessionResponse.builder()
                .publicId(session.getPublicId())
                .guestToken(newGuestToken)
                .customerName(guest ? getGuestDisplayName(session) : account.getFullName())
                .customerEmail(guest ? null : account.getEmail())
                .customerPhone(guest ? null : account.getPhone())
                .guest(guest)
                .status(session.getStatus())
                .lastMessagePreview(session.getLastMessagePreview())
                .lastMessageAt(session.getLastMessageAt())
                .customerUnreadCount(safeCount(session.getCustomerUnreadCount()))
                .adminUnreadCount(safeCount(session.getAdminUnreadCount()))
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private RealtimeChatMessageResponse mapMessage(RealtimeChatMessage message) {
        return RealtimeChatMessageResponse.builder()
                .messageId(message.getChatMessageId())
                .sessionPublicId(message.getSession().getPublicId())
                .senderType(message.getSenderType())
                .senderName(getSenderName(message))
                .content(message.getContent())
                .readByCustomer(message.getReadByCustomer())
                .readByAdmin(message.getReadByAdmin())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private String getSenderName(RealtimeChatMessage message) {
        if (message.getSenderType() == RealtimeChatSenderType.ADMIN) {
            return "Cửa hàng";
        }

        Account senderAccount = message.getSenderAccount();
        if (senderAccount != null && senderAccount.getFullName() != null) {
            return senderAccount.getFullName();
        }

        return getGuestDisplayName(message.getSession());
    }

    private String getSenderName(RealtimeChatSession session, RealtimeChatSenderType senderType) {
        if (senderType == RealtimeChatSenderType.ADMIN) {
            return "Cửa hàng";
        }

        Account account = session.getAccount();
        if (account != null && account.getFullName() != null) {
            return account.getFullName();
        }

        return getGuestDisplayName(session);
    }

    private String getGuestDisplayName(RealtimeChatSession session) {
        Long id = session.getChatSessionId();
        return "Khách hàng" + (id == null ? "" : " " + id);
    }

    private RealtimeChatSenderType normalizeTypingSenderType(
            RealtimeChatSession session,
            RealtimeChatTypingRequest request
    ) {
        RealtimeChatSenderType requested = request != null ? request.getSenderType() : null;

        if (requested == RealtimeChatSenderType.ADMIN) {
            return RealtimeChatSenderType.ADMIN;
        }

        return session.getAccount() != null
                ? RealtimeChatSenderType.CUSTOMER
                : RealtimeChatSenderType.GUEST;
    }

    private String normalizeContent(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung tin nhắn là bắt buộc");
        }

        if (normalized.length() > MAX_CONTENT_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung tin nhắn quá dài");
        }

        return normalized;
    }

    private String trimPreview(String content) {
        if (content.length() <= PREVIEW_LENGTH) {
            return content;
        }

        return content.substring(0, PREVIEW_LENGTH);
    }

    private String generateGuestToken() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }

    private String normalizeToken(String token) {
        String normalized = token == null ? "" : token.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String hashGuestToken(String guestToken) {
        String normalized = normalizeToken(guestToken);
        if (normalized == null) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể mã hóa token khách");
        }
    }

    private boolean sameAccount(Account left, Account right) {
        if (left == null || right == null) {
            return false;
        }

        return Objects.equals(left.getAccountId(), right.getAccountId());
    }

    private int safeCount(Integer value) {
        return value == null ? 0 : value;
    }
}
