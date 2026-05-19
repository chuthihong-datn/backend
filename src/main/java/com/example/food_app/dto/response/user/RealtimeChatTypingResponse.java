package com.example.food_app.dto.response.user;

import com.example.food_app.entity.enums.RealtimeChatSenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RealtimeChatTypingResponse {
    private String sessionPublicId;
    private RealtimeChatSenderType senderType;
    private String senderName;
    private Boolean typing;
}
