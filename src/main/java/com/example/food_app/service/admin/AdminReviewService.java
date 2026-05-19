package com.example.food_app.service.admin;

import com.example.food_app.dto.response.admin.ReviewResponse;
import com.example.food_app.entity.Review;
import com.example.food_app.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReviewService {
    private final ReviewRepository reviewRepository;

    public List<ReviewResponse> getReviews(Long menuId, Float rating) {

        List<Review> reviews;

        if (menuId != null && rating != null) {
            reviews = reviewRepository.findByMenu_MenuIdAndRating(menuId, rating);
        } else if (menuId != null) {
            reviews = reviewRepository.findByMenu_MenuId(menuId);
        } else if (rating != null) {
            reviews = reviewRepository.findByRating(rating);
        } else {
            reviews = reviewRepository.findAll();
        }

        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void hideReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy review"));

        review.setIsDeleted(true);
        reviewRepository.save(review);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .menuName(review.getMenu().getName())
                .userName(review.getAccount().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .isDeleted(review.getIsDeleted())
                .build();
    }
}