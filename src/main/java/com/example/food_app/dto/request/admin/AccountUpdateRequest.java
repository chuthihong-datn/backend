package com.example.food_app.dto.request.admin;

import com.example.food_app.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountUpdateRequest {
    private String fullName;
    private Role role;
}
