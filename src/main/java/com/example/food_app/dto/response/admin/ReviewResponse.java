package com.example.food_app.dto.response.admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse {
    private BigInteger reviewId;
    private String menuName;
    private String userName;
    private Float rating;
    private String comment;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
}

