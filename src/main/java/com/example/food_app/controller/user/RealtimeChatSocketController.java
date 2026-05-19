package com.example.food_app.controller.user;

import com.example.food_app.dto.request.user.RealtimeChatTypingRequest;
import com.example.food_app.service.user.RealtimeChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RealtimeChatSocketController {
    private final RealtimeChatService realtimeChatService;

    @MessageMapping("/realtime-chat/{publicId}/typing")
    public void publishTyping(
            @DestinationVariable String publicId,
            @Payload RealtimeChatTypingRequest request
    ) {
        realtimeChatService.publishTyping(publicId, request);
    }
}
