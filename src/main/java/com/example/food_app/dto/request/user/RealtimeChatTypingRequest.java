package com.example.food_app.dto.request.user;

import com.example.food_app.entity.enums.RealtimeChatSenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RealtimeChatTypingRequest {
    private RealtimeChatSenderType senderType;
    private Boolean typing;
    private String guestToken;
}
