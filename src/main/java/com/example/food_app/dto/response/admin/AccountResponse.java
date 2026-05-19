package com.example.food_app.dto.response.admin;

import com.example.food_app.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponse {
    private Long accountId;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private Boolean isActive;
    private String avtUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
