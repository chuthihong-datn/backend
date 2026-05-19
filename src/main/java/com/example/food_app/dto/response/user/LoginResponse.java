package com.example.food_app.dto.response.user;

import com.example.food_app.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private Long id;
    private String fullName;
    private String accessToken;
    private String refreshToken;
    private String email;
    private String phone;
    private Role role;
}