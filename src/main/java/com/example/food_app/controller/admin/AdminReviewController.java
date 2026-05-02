package com.example.food_app.controller.admin;

import com.example.food_app.dto.response.admin.ReviewResponse;
import com.example.food_app.service.admin.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService reviewService;

    @GetMapping
    public List<ReviewResponse> getReviews(
            @RequestParam(required = false) BigInteger menuId,
            @RequestParam(required = false) Float rating
    ) {
        return reviewService.getReviews(menuId, rating);
    }

    @PutMapping("/{id}/hide")
    public String hideReview(@PathVariable BigInteger id) {
        reviewService.hideReview(id);
        return "Đã ẩn review";
    }

    @DeleteMapping("/{id}")
    public String deleteReview(@PathVariable BigInteger id) {
        reviewService.hideReview(id);
        return "Đã ẩn review";
    }
}
