package com.example.food_app.controller.user;

import com.example.food_app.dto.request.user.RealtimeChatMessageRequest;
import com.example.food_app.dto.request.user.RealtimeChatSessionRequest;
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
@RequestMapping("/realtime-chat")
@RequiredArgsConstructor
public class RealtimeChatController {
    private final RealtimeChatService realtimeChatService;

    @PostMapping("/sessions")
    public ResponseEntity<RealtimeChatSessionResponse> createOrGetSession(
            @AuthenticationPrincipal Account account,
            @RequestBody(required = false) RealtimeChatSessionRequest request
    ) {
        String guestToken = request != null ? request.getGuestToken() : null;
        return ResponseEntity.ok(realtimeChatService.createOrGetSession(account, guestToken));
    }

    @GetMapping("/sessions/{publicId}/messages")
    public ResponseEntity<List<RealtimeChatMessageResponse>> getMessages(
            @PathVariable String publicId,
            @AuthenticationPrincipal Account account,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken
    ) {
        return ResponseEntity.ok(realtimeChatService.getMessages(publicId, account, guestToken));
    }

    @PostMapping("/sessions/{publicId}/messages")
    public ResponseEntity<RealtimeChatMessageResponse> sendMessage(
            @PathVariable String publicId,
            @AuthenticationPrincipal Account account,
            @RequestBody RealtimeChatMessageRequest request
    ) {
        return ResponseEntity.ok(
                realtimeChatService.sendParticipantMessage(publicId, account, request)
        );
    }

    @PatchMapping("/sessions/{publicId}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable String publicId,
            @AuthenticationPrincipal Account account,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken
    ) {
        realtimeChatService.markParticipantRead(publicId, account, guestToken);
        return ResponseEntity.noContent().build();
    }
}
