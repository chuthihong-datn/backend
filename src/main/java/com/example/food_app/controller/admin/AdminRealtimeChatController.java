package com.example.food_app.controller.admin;

import com.example.food_app.dto.request.admin.RealtimeChatStatusRequest;
import com.example.food_app.dto.request.user.RealtimeChatMessageRequest;
import com.example.food_app.dto.response.user.RealtimeChatMessageResponse;
import com.example.food_app.dto.response.user.RealtimeChatSessionResponse;
import com.example.food_app.entity.Account;
import com.example.food_app.service.user.RealtimeChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/realtime-chat")
@RequiredArgsConstructor
public class AdminRealtimeChatController {
    private final RealtimeChatService realtimeChatService;

    @GetMapping("/sessions")
    public ResponseEntity<List<RealtimeChatSessionResponse>> getSessions() {
        return ResponseEntity.ok(realtimeChatService.getAdminSessions());
    }

    @GetMapping("/sessions/{publicId}/messages")
    public ResponseEntity<List<RealtimeChatMessageResponse>> getMessages(
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(realtimeChatService.getAdminMessages(publicId));
    }

    @PostMapping("/sessions/{publicId}/messages")
    public ResponseEntity<RealtimeChatMessageResponse> sendMessage(
            @PathVariable String publicId,
            @AuthenticationPrincipal Account admin,
            @RequestBody RealtimeChatMessageRequest request
    ) {
        return ResponseEntity.ok(realtimeChatService.sendAdminMessage(publicId, admin, request));
    }

    @PatchMapping("/sessions/{publicId}/read")
    public ResponseEntity<Void> markRead(@PathVariable String publicId) {
        realtimeChatService.markAdminRead(publicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/sessions/{publicId}/status")
    public ResponseEntity<RealtimeChatSessionResponse> updateStatus(
            @PathVariable String publicId,
            @RequestBody RealtimeChatStatusRequest request
    ) {
        return ResponseEntity.ok(realtimeChatService.updateStatus(publicId, request.getStatus()));
    }
}
