package com.example.food_app.dto.response.user;

import com.example.food_app.entity.enums.RealtimeChatSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RealtimeChatSessionResponse {
    private String publicId;
    private String guestToken;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Boolean guest;
    private RealtimeChatSessionStatus status;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private Integer customerUnreadCount;
    private Integer adminUnreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
