package com.example.food_app.service.user;

import com.example.food_app.dto.request.user.LoginRequest;
import com.example.food_app.dto.request.user.RegisterRequest;
import com.example.food_app.dto.response.user.LoginResponse;
import com.example.food_app.dto.response.user.RegisterResponse;
import com.example.food_app.entity.Account;
import com.example.food_app.entity.Cart;
import com.example.food_app.entity.enums.Role;
import com.example.food_app.repository.AccountRepository;
import com.example.food_app.repository.CartRepository;
import com.example.food_app.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository accountRepository;
    private final CartRepository cartRepository;
    private final JwtUtil jwtUtil;

    public ResponseEntity<?> login(LoginRequest request) {
        try {

            Optional<Account> optionalAccount =
                    accountRepository.findByEmail(request.getEmail());

            if (optionalAccount.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("Email không tồn tại");
            }

            Account account = optionalAccount.get();

            if (!Boolean.TRUE.equals(account.getIsActive())) {
                return ResponseEntity
                        .badRequest()
                        .body("Tài khoản đã bị vô hiệu hóa");
            }

            if (!account.getPassword().equals(request.getPassword())) {
                return ResponseEntity
                        .badRequest()
                        .body("Mật khẩu không đúng");
            }

            String token = jwtUtil.generateToken(
                    account.getEmail(),
                    account.getRole()
            );

            LoginResponse response = LoginResponse.builder()
                    .id(account.getAccountId())
                    .fullName(account.getFullName())
                    .token(token)
                    .phone(account.getPhone())
                    .email(account.getEmail())
                    .role(account.getRole())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> register(RegisterRequest request) {
        try {

            if (accountRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("Email đã tồn tại")
                                .build());
            }

            if (!request.getPhone().matches("^0\\d{9}$")) {
                return ResponseEntity
                        .badRequest()
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("Số điện thoại không hợp lệ")
                                .build());
            }

            if (request.getPassword().length() < 6 ||
                    !request.getPassword().matches(
                            "^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>?,./]).+$")) {

                return ResponseEntity
                        .badRequest()
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("Mật khẩu phải >=6 ký tự và có chữ hoa, chữ thường, ký tự đặc biệt")
                                .build());
            }

            Account account = new Account();
            account.setFullName(request.getFullName());
            account.setEmail(request.getEmail());
            account.setPhone(request.getPhone());
            account.setPassword(request.getPassword());
            account.setRole(Role.CUSTOMER);
            account.setIsActive(true);
            accountRepository.save(account);

            Cart cart = new Cart();
            cart.setAccount(account);
            cartRepository.save(cart);

            return ResponseEntity.ok(
                    RegisterResponse.builder()
                            .success(true)
                            .message("Đăng ký thành công")
                            .build()
            );

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(RegisterResponse.builder()
                            .success(false)
                            .message("Lỗi hệ thống")
                            .build());
        }
    }
}
