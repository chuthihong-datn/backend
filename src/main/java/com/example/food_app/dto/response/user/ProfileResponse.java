package com.example.food_app.dto.response.user;

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
public class ProfileResponse {
    private BigInteger accountId;
    private String fullName;
    private String email;
    private String phone;
    private String avtUrl;
    private LocalDateTime createdAt;
}
