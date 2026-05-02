package com.example.food_app.controller.user;

import com.example.food_app.dto.request.user.ChatRequest;
import com.example.food_app.dto.response.user.ChatResponse;
import com.example.food_app.service.user.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String reply = chatService.chat(request.getMessage());
        ChatResponse response = ChatResponse.builder()
                .reply(reply)
                .build();
        return ResponseEntity.ok(response);
    }
}