package com.example.food_app.dto.response.user;

import com.example.food_app.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private BigInteger id;
    private String fullName;
    private String token;
    private String email;
    private String phone;
    private Role role;
}