package com.example.food_app.controller.user;

import com.example.food_app.dto.request.user.CreateReviewRequest;
import com.example.food_app.entity.Account;
import com.example.food_app.service.user.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal Account account
    ) {

        reviewService.createReview(request, account);

        return ResponseEntity.ok("Đánh giá thành công");
    }
}
