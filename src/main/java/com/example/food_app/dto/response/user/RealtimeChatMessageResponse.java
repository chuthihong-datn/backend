package com.example.food_app.dto.response.user;

import com.example.food_app.entity.enums.RealtimeChatSenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RealtimeChatMessageResponse {
    private Long messageId;
    private String sessionPublicId;
    private RealtimeChatSenderType senderType;
    private String senderName;
    private String content;
    private Boolean readByCustomer;
    private Boolean readByAdmin;
    private LocalDateTime createdAt;
}
